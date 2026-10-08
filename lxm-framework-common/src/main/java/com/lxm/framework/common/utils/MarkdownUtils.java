package com.lxm.framework.common.utils;

import java.util.Arrays;
import java.util.Objects;

public class MarkdownUtils {

    private static final String SUBJECT_LEVEL_1 = "# ";
    private static final String SUBJECT_LEVEL_2 = "## ";
    private static final String SUBJECT_LEVEL_3 = "### ";
    private static final String SUBJECT_LEVEL_4 = "#### ";
    private static final String SUBJECT_LEVEL_5 = "##### ";
    private static final String NEW_LINE = " \n";
    private static final String QUOTE = "```";

    private MarkdownUtils() {}

    public static Md init() {
        return new Md();
    }

    enum QuoteStyle {
        JAVA("java"),
        JS("javascript"),
        PYTHON("python"),
        JSON("json");
        private final String name;

        QuoteStyle(String name) {
            this.name = name;
        }
    }

    private static class Md {
        private Md() {
            this.content = new StringBuilder();
        }

        private final StringBuilder content;

        /**
         * 标题
         *
         * @param level   几级目录, default # 1
         * @param subject 标题内容
         * @return
         */
        public Md subject(int level, String subject) {
            if (1 == level) {
                content.append(SUBJECT_LEVEL_1);
            } else if (2 == level) {
                content.append(SUBJECT_LEVEL_2);
            } else if (3 == level) {
                content.append(SUBJECT_LEVEL_3);
            } else if (4 == level) {
                content.append(SUBJECT_LEVEL_4);
            } else if (5 == level) {
                content.append(SUBJECT_LEVEL_5);
            } else {
                content.append(SUBJECT_LEVEL_1);
            }
            content.append(subject).append(NEW_LINE);
            return this;
        }

        /**
         * 普通文本
         *
         * @param text 文本内容
         * @return
         */
        public Md text(String text) {
            content.append(text).append(NEW_LINE);
            return this;
        }

        /**
         * @return
         */
        public Md newLine() {
            content.append(NEW_LINE);
            return this;
        }

        /**
         * 加粗
         *
         * @param text 粗体内容
         * @return
         */
        public Md bold(String text) {
            content.append("**").append(text).append("**").append(NEW_LINE);
            return this;
        }

        /**
         * 行引用
         *
         * @param text 引用内容
         * @return
         */
        public Md backticks(String text) {
            content.append("`").append(text).append("`").append(NEW_LINE);
            return this;
        }

        /**
         * 无序列表
         *
         * @param lists 列表内容
         * @return
         */
        public Md list(String... lists) {
            String tag = "* ";
            Arrays.stream(lists).forEach(one -> content.append(tag).append(one).append(NEW_LINE));
            content.append(NEW_LINE);
            return this;
        }

        /**
         * 段落引用
         *
         * @param text       引用内容
         * @param quoteStyle 段落样式
         * @return
         */
        public Md quote(String text, QuoteStyle quoteStyle) {
            if (Objects.nonNull(quoteStyle)) {
                content.append(QUOTE)
                        .append(quoteStyle.name)
                        .append(NEW_LINE)
                        .append(text)
                        .append(QUOTE)
                        .append(NEW_LINE);
            } else {
                content.append(QUOTE).append(NEW_LINE).append(text).append(QUOTE).append(NEW_LINE);
            }
            return this;
        }

        /**
         * to-string
         *
         * @return
         */
        public String getContent() {
            return content.toString();
        }
    }
}
