import {
  EnigmaClient,
  ProtocolError,
  BusinessError,
  canonical,
  base64url,
  decode64,
} from "./enigma.js";
const status = document.querySelector("#status");
let client;
function output(text) {
  status.textContent += `${text}\n`;
}
function assert(value, message) {
  if (!value) throw new Error(message);
}
async function rejects(action) {
  try {
    await action();
  } catch (error) {
    assert(error instanceof ProtocolError || error.name === "OperationError", "错误类型不正确");
    return;
  }
  throw new Error("篡改响应被接受");
}
async function session() {
  client = new EnigmaClient({ authorization: `Bearer ${document.querySelector("#token").value}` });
  await client.session();
  output("PASS · 会话建立，四把密钥已导入内存");
}
async function fixedVectors() {
  const vector = await (await fetch("vectors.json", { cache: "no-store" })).json();
  assert(canonical(vector.canonicalInput) === vector.canonicalExpected, "JCS 固定向量不符");
  const hmac = await crypto.subtle.importKey(
    "raw",
    decode64(vector.requestSigning, 32),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"],
  );
  const signature = base64url(
    await crypto.subtle.sign("HMAC", hmac, new TextEncoder().encode(canonical(vector.signContext))),
  );
  assert(signature === vector.signature, "HMAC 固定向量不符");
  const aes = await crypto.subtle.importKey(
    "raw",
    decode64(vector.requestEncryption, 32),
    "AES-GCM",
    false,
    ["encrypt"],
  );
  const ciphertext = base64url(
    await crypto.subtle.encrypt(
      {
        name: "AES-GCM",
        iv: decode64(vector.iv, 12),
        additionalData: new TextEncoder().encode(canonical(vector.encryptContext)),
        tagLength: 128,
      },
      aes,
      new TextEncoder().encode(canonical(vector.plaintext)),
    ),
  );
  assert(ciphertext === vector.ciphertext, "GCM 固定向量不符");
  output("PASS · JCS / HMAC / AES-GCM 跨语言固定向量");
}
async function run() {
  status.textContent = "";
  await fixedVectors();
  await session();
  const parameters = await client.call("PUT", "/demo/params?q=%E4%B8%AD%E6%96%87&q=%2B&q=&bare");
  assert(
    JSON.stringify(parameters.q) === JSON.stringify(["中文", "+", ""]) && parameters.bare[0] === "",
    "query 参数不符",
  );
  output("PASS · 无 body 的 PUT 参数验签：重复值、中文、加号、空值");
  const data = await client.call("POST", "/demo/body", {
    body: { name: "中文", secret: "hidden" },
  });
  assert(data.name === "中文" && !Object.hasOwn(data, "secret"), "响应字段过滤不符");
  output("PASS · JSON 加解密，响应先过滤再加密");
  const prepared = await client.prepare("POST", "/demo/body", {
    body: { name: "篡改测试", secret: "hidden" },
  });
  const response = await fetch(prepared.url, prepared.options);
  const bytes = await response.arrayBuffer();
  await rejects(() =>
    client.verify(prepared, new Response(bytes, { status: 202, headers: response.headers })),
  );
  const altered = JSON.parse(new TextDecoder().decode(bytes));
  altered.message = "forged";
  await rejects(() =>
    client.verify(
      prepared,
      new Response(canonical(altered), { status: response.status, headers: response.headers }),
    ),
  );
  await client.verify(
    prepared,
    new Response(bytes, { status: response.status, headers: response.headers }),
  );
  await rejects(() =>
    client.verify(
      prepared,
      new Response(bytes, { status: response.status, headers: response.headers }),
    ),
  );
  output("PASS · HTTP status / message 篡改和重复响应被拒绝");
  const signed = await client.prepare("GET", "/demo/params/42?q=replay");
  await client.verify(signed, await fetch(signed.url, signed.options));
  const replay = await fetch(signed.url, signed.options);
  assert(replay.status === 409, "服务端接受了重放");
  output("PASS · 服务端拒绝同一 nonce 的重复请求");
  try {
    await client.call("POST", "/demo/body", { body: { name: "", secret: "hidden" } });
    throw new Error("DTO 校验未执行");
  } catch (error) {
    assert(error instanceof BusinessError && error.code === 400, "DTO 错误未认证");
  }
  output("PASS · 解密后 DTO 校验，错误响应验签后读取");
  const before = await client.prepare("GET", "/demo/params/42?q=inflight");
  const oldResponse = await fetch(before.url, before.options);
  await client.refresh();
  await client.verify(before, oldResponse);
  await client.call("GET", "/demo/params/42?q=refreshed");
  output("PASS · 刷新与旧 kid 在途响应");
  const revoked = await client.prepare("GET", "/demo/params/42?q=revoked");
  await client.revoke();
  assert((await fetch(revoked.url, revoked.options)).status === 403, "撤销会话仍被接受");
  output("PASS · 撤销后拒绝新准入");
  output("全部验证通过");
  status.dataset.result = "passed";
}
for (const [id, action] of Object.entries({
  session,
  run,
  refresh: async () => {
    await client.refresh();
    output("密钥已刷新");
  },
  revoke: async () => {
    await client.revoke();
    output("会话已撤销");
  },
})) {
  document.querySelector(`#${id}`).addEventListener("click", async () => {
    const buttons = [...document.querySelectorAll("button")];
    buttons.forEach((button) => {
      button.disabled = true;
    });
    delete status.dataset.result;
    try {
      await action();
    } catch (error) {
      output(`FAIL · ${error.message}`);
      status.dataset.result = "failed";
    } finally {
      buttons.forEach((button) => {
        button.disabled = false;
      });
    }
  });
}
