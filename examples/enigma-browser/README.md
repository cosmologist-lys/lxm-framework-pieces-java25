# Enigma 浏览器示例

本示例演示 Java 服务端与浏览器 Web Crypto 的实际互通：会话签发、参数验签、JSON 加解密、响应认证、刷新和撤销。`enigma.js` 无运行时 npm 依赖；Playwright 仅用于开发测试。

## 启动演示

使用 JDK 25，在仓库根目录执行：

```bash
./mvnw verify
node scripts/enigma-demo.mjs 18888
```

打开 `http://127.0.0.1:18888/`，点击“运行全部验证”。示例覆盖固定向量、中文/重复 query 值、JSON body、DTO 校验、响应字段过滤、篡改、nonce 重放、刷新与在途响应、撤销。

演示 Java 应用位于 `lxm-framework-enigma/src/test`，不进入发布 JAR。它使用内存会话和公开测试凭证 `Bearer browser-test`，只绑定本机回环地址；生产应用需要自己的认证与 HTTPS 配置。

## 在前端使用客户端

```javascript
import { EnigmaClient, BusinessError, ProtocolError } from "./enigma.js";

const client = new EnigmaClient({
  origin: location.origin,
  authorization: `Bearer ${accessToken}`,
  csrfToken,
  timeout: 15000,
});
await client.session();

try {
  await client.call("POST", "/protected/body", {
    mode: "ENCRYPT",
    body: { name: "中文" },
  });
  await client.call("GET", "/protected/params?name=中文&tag=a&tag=b", {
    mode: "SIGN",
  });
} catch (error) {
  if (error instanceof BusinessError) {
    // 该业务消息已通过响应认证，可以向用户展示。
    showMessage(error.message);
  } else if (error instanceof ProtocolError) {
    showMessage("响应验证失败");
  } else {
    // 网络、超时等错误按应用自己的规则处理。
    throw error;
  }
}
```

`accessToken/csrfToken/showMessage` 来自应用自己的登录状态和 UI。仅使用 Bearer 时可以省略 csrfToken；Cookie 认证必须提供服务器会话中的可信 CSRF token。

| 方法 | 行为 |
| --- | --- |
| `session()` | 获取本标签页 sid/kid，并导入四把密钥 |
| `call(method, path, options)` | 构造请求、发送、验证并解密响应；非零业务码抛 `BusinessError` |
| `refresh()` | 刷新当前 kid，保留需要验证在途响应的旧 key |
| `revoke()` | 撤销当前 sid 并清理客户端会话状态 |
| `prepare()/verify()` | 低层接口，供需要自己发送请求的集成使用 |

没有 body 时默认 SIGN，有 body 时默认 ENCRYPT；服务器接口仍必须明确允许该模式。服务端会拒绝模式不符或缺少协议字段的请求。

## 密钥与错误处理

四把密钥只导入为内存中的不可导出 CryptoKey。页面刷新、退出登录或更换账号后重新建立会话，不把密钥存到 localStorage/Cookie，也不打印到日志。

客户端不会自动定时刷新。应用按部署的 `refresh-after-millis` 定时调用 `refresh()`，默认建议在签发后 12 分钟刷新；达到使用量上限也需要刷新。原始签发响应包含 `refreshAt`，当前 `session()/refresh()` 返回 sid、kid、expiresAt；不要从不存在的客户端字段读取建议时间。刷新控制操作在旧 kid 的宽限期内幂等，可以有限重试；业务请求不会自动重试。写请求准入后响应丢失时，需要业务幂等键或查询状态，不能重用已登记 nonce。

先认证响应再展示 `message`。无法认证的错误、被篡改响应和网络故障不能伪装成可信业务结果。

## 地址与跨域

请求 URL 必须属于客户端配置的 `origin`，不接受 userinfo 或任意重定向。HTTPS 默认必需；本机演示明确开启 localhost 例外。

跨域部署在服务端配置精确 `allowed-origins`，允许所需协议请求 header，并暴露 `X-Enigma-*` 响应 header；Cookie 使用凭据请求和 CSRF。代理改写路径时，服务端 `public-path-prefix` 必须与浏览器请求的真实外部路径一致。

## 运行浏览器测试

```bash
cd examples/enigma-browser
npm ci
npx playwright install chromium
npm run check
npm test
```

先完成根 reactor 的 verify，以生成测试 classpath。Playwright 启动真实 Java 示例应用，浏览器使用原生 Web Crypto；本机测试默认占用 18889，两个 JDK 仓库的浏览器测试应依次运行，或各自指定独立端口。

`vectors.json` 与 Java 测试资源相同，包含公开固定测试密钥，不能用于部署。它们由 `org.example.GenerateEnigmaVectors` 生成；更改协议时同时核对独立标准向量，不通过重写向量掩盖实现错误。

服务端接入见 [Enigma README](../../lxm-framework-enigma/README.md)，消息格式见[协议](../../docs/enigma-protocol.md)。
