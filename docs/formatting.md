# 格式约定

Java 与 POM 使用 4 空格缩进；浏览器 JS/MJS、HTML、CSS、YAML 使用 2 空格。代码块展开到独立行，避免把多个语句、依赖或 XML 元素压成一行。`.editorconfig` 保存编辑器约定。

Java 使用 google-java-format 1.37.0 的 AOSP 风格，默认列宽 100。保留 import 的原有顺序和内容，不重排修饰符，不改写字符串与 Javadoc。下载官方 `all-deps` JAR 后，在仓库根目录执行：

```bash
rg --files -g '*.java' -g '!**/target/**' > /tmp/lfp-java-files.txt
java -jar /path/to/google-java-format-1.37.0-all-deps.jar \
    --aosp --skip-sorting-imports --skip-removing-unused-imports \
    --skip-reflowing-long-strings --skip-javadoc-formatting --skip-reordering-modifiers \
    --replace @/tmp/lfp-java-files.txt
```

检查是否仍需格式化时，把 `--replace` 换为 `--dry-run --set-exit-if-changed`。工具及参数见[官方说明](https://github.com/google/google-java-format)。

POM 保留元素顺序、命名空间、注释和有效值，按 4 空格展开每个元素；项目开始标签的长属性分行。用 XML 格式化功能时只调整布局，不能修改依赖或解析值。

浏览器及工作流使用固定 Prettier 3.9.9，列宽 100：

```bash
npx --yes prettier@3.9.9 --write --print-width 100 \
    'examples/enigma-browser/**/*.js' 'examples/enigma-browser/*.html' \
    'examples/enigma-browser/*.css' 'scripts/*.mjs' '.github/workflows/*.yml'
```

检查时使用 `--check`。格式化工具只作为开发工具，不增加框架运行时依赖。

不要手工格式化生成的 Maven Wrapper、package-lock 或固定协议/历史 JSON 样本；这些文件通过各自生成器维护，样本的字节内容是验证的一部分。源代码格式调整后运行 `./mvnw verify`；涉及依赖版本时还需要相应服务集成和浏览器验证。
