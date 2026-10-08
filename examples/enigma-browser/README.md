# Web Crypto 最小示例

示例 SDK 无运行时依赖。Playwright 仅用于开发验证。Java 测试应用位于 enigma 的 src/test，不进入发布 JAR；Bearer browser-test 是公开的测试字符串。生产接入必须替换身份 SPI 和部署配置。

```bash
# 仓库根目录，构建并生成测试 classpath
./mvnw verify
node scripts/enigma-demo.mjs 18888
# 浏览器打开 http://127.0.0.1:18888/，点击“运行全部验证”
```

```bash
cd examples/enigma-browser
npm ci
npx playwright install chromium
npm run check
npm test
```

SDK：`new EnigmaClient({origin,authorization,csrfToken})`，`await client.session()`；`call(method,path,{mode:"ENCRYPT",body:{...}})`、`refresh()`、`revoke()`。没有 body 时默认 SIGN，有 body 时默认 ENCRYPT；接口模式由服务器声明，不能自动降级。示例 button 完成固定向量、UTF-8/重复参数、POST 解密/DTO/过滤、响应篡改、重放、密钥刷新和撤销。

四把密钥只保存在内存，导入为不可导出 CryptoKey；不写 localStorage/cookie，不打印。页面刷新或重新登录重新建立会话；收到 refreshAt 后应用主动调用 refresh，达到使用量上限也需要刷新。示例不自动重试业务请求：请求已准入但响应丢失时，nonce 不能再用，业务写入需独立幂等键或人工查询状态。refresh 同一旧 kid 在宽限期内幂等，可重试该控制操作。旧 kid 在途响应仍可验证，完成后淘汰旧密钥。

URL 只能同源，无 userinfo；TLS 默认必需，仅开发 localhost 例外。代理改写路径时服务器配置 public-path-prefix，客户端签名的是外部实际 path。跨域使用精确 Origin 白名单，暴露所有 X-Enigma 响应头，Cookie 还需要可信 X-CSRF-Token。

vectors.json 与 Java src/test/resources/enigma-vectors.json 完全一致，包含公开固定测试密钥，不能用于部署。变更协议后在已构建 reactor 生成测试 classpath，再运行 `org.example.GenerateEnigmaVectors`；勿通过更新向量掩盖实现错误。HKDF 同时有独立 RFC 5869 已知结果测试。
