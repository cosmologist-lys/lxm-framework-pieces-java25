# lxm-framework-excel

`excel` 基于 Apache POI 提供注解式 Excel 导入导出、多 sheet、HTML 转换和 Spring MVC 文件下载。适合列表报表、后台批量导入和模板式数据交换；支持 xls/xlsx，默认导出 xlsx。

## 引入依赖

要求 JDK 25。先按[项目说明](../README.md)构建制品并配置应用的 parent，然后引入：

```xml
<dependency>
    <groupId>com.lxm</groupId>
    <artifactId>lxm-framework-excel</artifactId>
    <version>2.0.0-java25-SNAPSHOT</version>
</dependency>
```

## 定义导出/导入模型

```java
import com.lxm.framework.excel.annotation.Excel;

public class UserRow {
    @Excel(name = "姓名")
    private String name;

    public UserRow() {}

    public UserRow(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

字段以 `@Excel` 标记表头等信息。导入模型应有可用的无参数构造器和 setter；导出需要 getter。集合/嵌套数据可按 `@ExcelCollection/@ExcelEntity` 的约定建模。

## 导出到文件

```java
import com.lxm.framework.excel.entity.ExportParams;
import com.lxm.framework.excel.util.ExcelUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

var parameters = new ExportParams();
parameters.setSheetName("用户");
parameters.setTitle("用户列表");
try (var workbook =
                ExcelUtils.export(parameters, UserRow.class, List.of(new UserRow("中文")));
        var output = Files.newOutputStream(Path.of("users.xlsx"))) {
    workbook.write(output);
}
```

导出得到的 `Workbook` 归调用方所有，写出后关闭。`ExportParams` 控制 sheet、标题、表头、样式与 xls/xlsx 类型；修改后复用参数时避免跨线程共享。

## 多 sheet 和 MVC 下载

```java
import com.lxm.framework.excel.entity.ExcelMultiSheets;

var sheets = new ExcelMultiSheets();
sheets.appendSheet("用户", UserRow.class, List.of(new UserRow("用户 A")));
sheets.appendSheet("管理员", UserRow.class, List.of(new UserRow("用户 B")));
try (var workbook = ExcelUtils.exportMultiSheets(sheets)) {
    // 写入文件或应用拥有的输出流。
}
```

同一个 Workbook 的多个 sheet 顺序构建，不能把 POI 对象放入并行任务共同写入。

Controller 下载可以返回 `new ExcelView("用户列表", workbook)`，或使用直接接收模型/数据的构造器。`ExcelView` 设置文件类型及中文文件名的 `Content-Disposition`，完成一次渲染后关闭 Workbook；交给 View 后不要提前关闭或重复使用该工作簿。

## 读取行与解析模型

```java
// upload 是应用接收到的 MultipartFile。
var rows = ExcelUtils.read(upload, 0, 2, true);
```

参数分别是文件、sheet 下标、读取列数、是否跳过首行。检测实际文件格式，空单元格/缺失行用空字符串填充，包含最后一行；读取后关闭内部流和 Workbook。

注解式模型解析使用：

```java
import com.lxm.framework.excel.entity.ImportParams;

var parameters = new ImportParams();
parameters.setTitleRows(1); // 对应上例的一行标题；无标题模板设为 0。
try (var input = Files.newInputStream(Path.of("users.xlsx"))) {
    var result = ExcelUtils.parse(input, UserRow.class, parameters);
    // result 中包含成功数据、错误信息及按配置返回的工作簿。
}
```

`parse` 的输入流由调用方管理。`moreInfo=false` 时关闭内部 Workbook；需要返回成功/失败工作簿时设置 `moreInfo=true`，调用方负责关闭返回的工作簿。表头、标题行和验证规则应与实际模板一致，具体属性见 `ImportParams`。

## HTML 和公式

`ExcelUtils.htmlToExcel(html)` 可以把支持的 HTML 表格转换为 Workbook。字符串 `=1+1` 默认作为普通文本写出，只有显式指定 `CellType.FORMULA` 才按公式处理。允许公式时，应用应自己控制公式来源及可引用的数据。

## 注意事项

- 当前使用内存工作簿，应用需要限制上传大小、行数、sheet 数量；不提供无限规模文件或大文件流式导出。
- Workbook、sheet、样式及导出 builder 不跨线程共享。
- 输入扩展名不决定格式；对上传来源仍需应用自己的大小与内容规则。
- 使用文件工具时明确输入流、输出流和 Workbook 的所有权，不能重复关闭容器输出流。
- 直接返回 `ExcelView` 属于文件响应，不适用于 Enigma JSON 保护接口。

回到[项目接入说明](../README.md)。
