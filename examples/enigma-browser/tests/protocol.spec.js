import { test, expect } from "@playwright/test";
test("Web Crypto 与 Java 完成协议闭环及负向验证", async ({ page }, testInfo) => {
  await page.goto("/");
  await page.getByRole("button", { name: "运行全部验证", exact: true }).click();
  await expect(page.locator("#status")).toHaveAttribute("data-result", "passed");
  await expect(page.locator("#status")).toContainText("全部验证通过");
  await page.screenshot({ path: testInfo.outputPath("protocol.png"), fullPage: true });
});
