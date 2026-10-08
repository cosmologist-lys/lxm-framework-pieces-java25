const encoder = new TextEncoder();
const decoder = new TextDecoder("utf-8", { fatal: true });
const MAX_BODY = 1_048_576;
const MAX_ENVELOPE = 1_572_864;

export class ProtocolError extends Error {
  constructor() { super("消息未通过加密协议验证"); this.name = "ProtocolError"; }
}
export class BusinessError extends Error {
  constructor(result) { super(result.message); this.name = "BusinessError"; this.code = result.code; }
}
function check(value) { if (!value) throw new ProtocolError(); }
function validUnicode(text) {
  for (let i = 0; i < text.length; i++) {
    const unit = text.charCodeAt(i);
    if (unit >= 0xd800 && unit <= 0xdbff) {
      const low = text.charCodeAt(++i);
      check(low >= 0xdc00 && low <= 0xdfff);
    } else check(unit < 0xdc00 || unit > 0xdfff);
  }
}
// RFC 8785 使用 UTF-16 键顺序。逐项写出，避免 JSON.stringify 对数字形键重新排序。
export function canonical(value, depth = 0) {
  check(depth <= 64);
  if (value === null) return "null";
  if (typeof value === "string") { validUnicode(value); return JSON.stringify(value); }
  if (typeof value === "boolean") return JSON.stringify(value);
  if (typeof value === "number") { check(Number.isFinite(value)); return JSON.stringify(value); }
  if (Array.isArray(value)) return `[${value.map(item => canonical(item, depth + 1)).join(",")}]`;
  check(typeof value === "object" && (Object.getPrototypeOf(value) === Object.prototype || Object.getPrototypeOf(value) === null));
  return `{${Object.keys(value).sort().map(key => `${canonical(key, depth + 1)}:${canonical(value[key], depth + 1)}`).join(",")}}`;
}
function validateNumbers(value, depth = 0) {
  check(depth <= 64);
  if (typeof value === "number") check(Number.isFinite(value) && (!Number.isInteger(value) || Number.isSafeInteger(value)));
  if (value && typeof value === "object") Object.values(value).forEach(item => validateNumbers(item, depth + 1));
}
export function base64url(bytes) {
  let binary = "";
  for (const byte of new Uint8Array(bytes)) binary += String.fromCharCode(byte);
  return btoa(binary).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}
export function decode64(value, size) {
  check(typeof value === "string" && /^[A-Za-z0-9_-]+$/.test(value));
  let binary;
  try { binary = atob(value.replaceAll("-", "+").replaceAll("_", "/") + "=".repeat((4 - value.length % 4) % 4)); }
  catch { throw new ProtocolError(); }
  const bytes = Uint8Array.from(binary, char => char.charCodeAt(0));
  check(base64url(bytes) === value && (size === undefined || bytes.length === size));
  return bytes;
}
function random(size) { return crypto.getRandomValues(new Uint8Array(size)); }
async function importKeys(raw) {
  const keys = {};
  for (const name of ["requestEncryption", "responseEncryption", "requestSigning", "responseSigning"]) {
    const bytes = decode64(raw[name], 32);
    const algorithm = name.endsWith("Encryption") ? { name: "AES-GCM", length: 256 } : { name: "HMAC", hash: "SHA-256" };
    const uses = name === "requestEncryption" ? ["encrypt"] : name === "responseEncryption" ? ["decrypt"] : name === "requestSigning" ? ["sign"] : ["verify"];
    keys[name] = await crypto.subtle.importKey("raw", bytes, algorithm, false, uses);
    bytes.fill(0);
  }
  return keys;
}
export function queryParams(url) {
  const parameters = Object.create(null);
  for (const [name, value] of url.searchParams) (parameters[name] ??= []).push(value);
  check(Object.keys(parameters).length <= 128 && [...url.searchParams].length <= 256 && url.search.length <= 8193);
  return parameters;
}
async function limitedBytes(response, maximum) {
  const reader = response.body?.getReader();
  if (!reader) return new Uint8Array();
  const chunks = []; let total = 0;
  try {
    for (;;) {
      const { value, done } = await reader.read();
      if (done) break;
      total += value.length;
      if (total > maximum) { await reader.cancel(); throw new ProtocolError(); }
      chunks.push(value);
    }
  } finally { reader.releaseLock(); }
  const bytes = new Uint8Array(total); let offset = 0;
  for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.length; }
  return bytes;
}
function parsedCanonical(bytes) {
  let text, value;
  try { text = decoder.decode(bytes); value = JSON.parse(text); }
  catch { throw new ProtocolError(); }
  check(canonical(value) === text); // 同时拒绝重复字段、非法 Unicode、额外 JSON 及非规范数字表示。
  validateNumbers(value); return value;
}

export class EnigmaClient {
  #versions = new Map();
  #current;
  #sid;
  #offset = 0;
  constructor({ origin = location.origin, authorization, csrfToken, timeout = 15000 }) {
    const parsed = new URL(origin); check(parsed.protocol === "https:" || ["localhost", "127.0.0.1", "[::1]"].includes(parsed.hostname));
    this.origin = parsed.origin; this.authorization = authorization; this.csrfToken = csrfToken; this.timeout = timeout;
  }
  #headers() {
    const headers = new Headers();
    if (this.authorization) headers.set("Authorization", this.authorization);
    if (this.csrfToken) headers.set("X-CSRF-Token", this.csrfToken);
    return headers;
  }
  async #grant(path, payload) {
    const headers = this.#headers();
    if (payload) headers.set("Content-Type", "application/json");
    const response = await fetch(this.origin + path, { method: "POST", headers, credentials: "include", cache: "no-store", redirect: "error", signal: AbortSignal.timeout(this.timeout), body: payload ? JSON.stringify(payload) : undefined });
    check(response.ok && response.headers.get("Cache-Control")?.includes("no-store"));
    const grant = JSON.parse(decoder.decode(await limitedBytes(response, 8192)));
    check(grant.v === 1 && /^[A-Za-z0-9_-]{22}$/.test(grant.sid) && /^[A-Za-z0-9_-]{22}$/.test(grant.kid));
    for (const name of ["serverTime", "refreshAt", "expiresAt"]) check(typeof grant[name] === "string" && /^[1-9][0-9]{0,15}$/.test(grant[name]));
    if (this.#sid) check(this.#sid === grant.sid);
    const imported = await importKeys(grant.keys);
    if (!this.#versions.has(grant.kid)) this.#versions.set(grant.kid, { keys: imported, ivs: new Set(), pending: 0, expiresAt: Number(grant.expiresAt) });
    this.#sid = grant.sid; this.#current = grant.kid; this.#offset = Number(grant.serverTime) - Date.now();
    return { sid: grant.sid, kid: grant.kid, expiresAt: grant.expiresAt };
  }
  async session() { check([...this.#versions.values()].every(version => version.pending === 0)); this.#versions.clear(); this.#sid = undefined; return this.#grant("/enigma/session"); }
  async refresh() { check(this.#sid && this.#current); return this.#grant("/enigma/session/refresh", { sid: this.#sid, currentKid: this.#current }); }
  async revoke() {
    check(this.#sid);
    const headers = this.#headers(); headers.set("Content-Type", "application/json");
    const response = await fetch(this.origin + "/enigma/session/revoke", { method: "POST", headers, credentials: "include", cache: "no-store", redirect: "error", signal: AbortSignal.timeout(this.timeout), body: JSON.stringify({ sid: this.#sid }) });
    check(response.ok); this.#versions.clear(); this.#sid = undefined; this.#current = undefined;
  }
  async prepare(method, path, { body, mode = body === undefined ? "SIGN" : "ENCRYPT" } = {}) {
    check(this.#sid && ["SIGN", "ENCRYPT"].includes(mode));
    const version = this.#versions.get(this.#current); check(version && Date.now() + this.#offset < version.expiresAt);
    check((mode === "SIGN") === (body === undefined));
    for (const [kid, retained] of this.#versions) if (kid !== this.#current && retained.pending === 0 && retained.expiresAt + this.timeout < Date.now() + this.#offset) this.#versions.delete(kid);
    const url = new URL(path, this.origin); check(url.origin === this.origin && !url.hash && !url.username && !url.password);
    const context = { v: 1, mode, direction: "request", sid: this.#sid, kid: this.#current, method: method.toUpperCase(), path: url.pathname,
      params: queryParams(url), contentType: mode === "ENCRYPT" ? "application/json" : "", timestamp: String(Date.now() + this.#offset), nonce: base64url(random(16)) };
    const headers = this.#headers();
    for (const [name, value] of Object.entries({ Version: "1", Mode: mode, Session: context.sid, Kid: context.kid, Timestamp: context.timestamp, Nonce: context.nonce })) headers.set(`X-Enigma-${name}`, value);
    let encodedBody;
    if (mode === "SIGN") headers.set("X-Enigma-Sign", base64url(await crypto.subtle.sign("HMAC", version.keys.requestSigning, encoder.encode(canonical(context)))));
    else {
      validateNumbers(body); const plaintext = encoder.encode(canonical(body)); check(plaintext.length <= MAX_BODY);
      let iv;
      do { iv = random(12); } while (version.ivs.has(base64url(iv)));
      check(version.ivs.size < 4096); version.ivs.add(base64url(iv));
      const ciphertext = await crypto.subtle.encrypt({ name: "AES-GCM", iv, additionalData: encoder.encode(canonical(context)), tagLength: 128 }, version.keys.requestEncryption, plaintext);
      encodedBody = JSON.stringify({ v: 1, kid: context.kid, iv: base64url(iv), ciphertext: base64url(ciphertext) });
      check(encoder.encode(encodedBody).length <= MAX_ENVELOPE); headers.set("Content-Type", "application/json");
    }
    return { url: url.href, context, completed: false, options: { method: context.method, headers, credentials: "include", cache: "no-store", redirect: "error", body: encodedBody, signal: AbortSignal.timeout(this.timeout) } };
  }
  async verify(prepared, response) {
    check(!prepared.completed);
    const headers = response.headers, request = prepared.context, version = this.#versions.get(request.kid);
    check(version && headers.get("X-Enigma-Version") === "1" && headers.get("X-Enigma-Session") === request.sid && headers.get("X-Enigma-Kid") === request.kid);
    const timestamp = headers.get("X-Enigma-Timestamp"), nonce = headers.get("X-Enigma-Nonce"), protection = headers.get("X-Enigma-Mode"), unexecuted = headers.get("X-Enigma-Unexecuted");
    check(/^[1-9][0-9]{0,15}$/.test(timestamp ?? "") && Math.abs(Number(timestamp) - (Date.now() + this.#offset)) <= 60000);
    decode64(nonce, 16); check(unexecuted === "true" || unexecuted === "false");
    const value = parsedCanonical(await limitedBytes(response, MAX_ENVELOPE));
    check(Number.isInteger(value.code) && value.code >= -2147483648 && value.code <= 2147483647 && typeof value.message === "string" && Object.hasOwn(value, "data"));
    const authentication = { v: 1, request, direction: "response", timestamp, nonce, status: response.status, code: value.code, message: value.message, protection, unexecuted: unexecuted === "true" };
    if (protection === "SIGN") {
      check(request.mode === "SIGN" || value.data === null); check(Object.keys(value).length === 4);
      authentication.data = value.data;
      check(await crypto.subtle.verify("HMAC", version.keys.responseSigning, decode64(value.sign, 32), encoder.encode(canonical(authentication))));
    } else {
      check(protection === "ENCRYPT" && request.mode === "ENCRYPT" && Object.keys(value).length === 3);
      const envelope = value.data; check(envelope?.v === 1 && envelope.kid === request.kid && Object.keys(envelope).length === 4);
      const ciphertext = decode64(envelope.ciphertext); check(ciphertext.length >= 16 && ciphertext.length <= MAX_BODY + 16);
      let plaintext;
      try { plaintext = await crypto.subtle.decrypt({ name: "AES-GCM", iv: decode64(envelope.iv, 12), additionalData: encoder.encode(canonical(authentication)), tagLength: 128 }, version.keys.responseEncryption, ciphertext); }
      catch { throw new ProtocolError(); }
      value.data = parsedCanonical(new Uint8Array(plaintext));
    }
    prepared.completed = true; return value;
  }
  async call(method, path, options) {
    const prepared = await this.prepare(method, path, options); const version = this.#versions.get(prepared.context.kid); version.pending++;
    try {
      const result = await this.verify(prepared, await fetch(prepared.url, prepared.options));
      if (result.code !== 0) throw new BusinessError(result);
      return result.data;
    } finally {
      version.pending--;
      if (prepared.context.kid !== this.#current && version.pending === 0) this.#versions.delete(prepared.context.kid);
    }
  }
}
