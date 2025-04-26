package org.jeffrey.core.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;

import java.util.Arrays;

/**
 * Utility class for Markdown processing
 */
public class MarkdownUtil {
    
    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;
    
    static {
        // 设置Markdown解析选项
        MutableDataSet options = new MutableDataSet();
        
        // 启用表格支持
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create()
        ));
        
        // 设置HTML渲染选项
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        
        // 初始化解析器和渲染器
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }
    
    /**
     * 将Markdown文本转换为HTML
     *
     * @param markdown Markdown格式的文本
     * @return 转换后的HTML文本
     */
    public static String convertToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        // 解析Markdown为AST
        Node document = PARSER.parse(markdown);
        
        // 渲染AST为HTML
        return RENDERER.render(document);
    }
    
    /**
     * 从Markdown文本中提取纯文本摘要
     *
     * @param markdown Markdown格式的文本
     * @param maxLength 最大长度
     * @return 提取的纯文本摘要
     */
    public static String extractPlainTextSummary(String markdown, int maxLength) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        // 转换为HTML
        String html = convertToHtml(markdown);
        
        // 去除HTML标签，只保留文本内容
        String plainText = html.replaceAll("<[^>]*>", "");
        
        // 截取指定长度的摘要
        if (plainText.length() <= maxLength) {
            return plainText;
        } else {
            return plainText.substring(0, maxLength) + "...";
        }
    }
} 