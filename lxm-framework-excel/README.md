# lxm-framework-excel

Excel 导入导出与 MVC 下载。Java 25 / Spring Boot 4.1.1；Maven 坐标 `com.lxm:lxm-framework-excel:2.0.0-java25-SNAPSHOT`。

## 功能与接入

提供 @Excel 注解、ExcelUtils、ExcelMultiSheets、HTML 转换与 ExcelView，依赖 POI 5.5.1。`ExcelUtils.export(params,Entry.class,data)` 返回调用方拥有的 Workbook，需 try-with-resources；字符串 =1+1 按文本输出，只有显式 CellType.FORMULA 允许公式。

## 使用约定与验证

read 使用 WorkbookFactory 检测内容格式，关闭文件流/Workbook，包含最后一行并处理空行/空单元格。多 sheet 顺序写同一 Workbook，禁止并发写 POI 对象。parse 的输入流由调用方关闭；moreInfo=false 关闭内部 Workbook；moreInfo=true 返回 success/fail Workbook，由调用方关闭。ExcelView 一次 render 后关闭传入 Workbook，不关闭容器输出流，支持中文 filename*。当前为有界内存工作簿处理，应用应限制上传体积/行数；大文件流式导出不是本版本功能。测试覆盖实际 xlsx 重读、错误扩展名、末行、20 sheet 与文本公式。

参见 [根说明](../README.md)、[迁移](../docs/migration-java17-to-java21.md) 和 [验证](../docs/verification.md)。
