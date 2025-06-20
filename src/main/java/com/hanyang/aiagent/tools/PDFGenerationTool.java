package com.hanyang.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.hanyang.aiagent.constant.FileConstant;
import com.itextpdf.layout.font.FontProvider;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PDF 生成工具
 */
public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            File pdfFile = new File(filePath);

            // 1. 设置转换属性和自定义字体
            ConverterProperties properties = new ConverterProperties();
            FontProvider fontProvider = new FontProvider();

            fontProvider.addFont("src/main/resources/fonts/SmileySans-Oblique.ttf");
            properties.setFontProvider(fontProvider);

            String css = "body { font-family: 'Smiley Sans', sans-serif; }";

            // 2. 解析 Markdown -> HTML
            Parser parser = Parser.builder().build();
            Node document = parser.parse(content);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            // 在HTML中注入CSS来指定默认字体
            String htmlContent = "<html><head><style>" + css + "</style></head><body>" + renderer.render(document) + "</body></html>";

            // 3. 使用带有自定义字体配置的转换器，将 HTML 一步转换为 PDF
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                HtmlConverter.convertToPdf(htmlContent, fos, properties);
            }

            // 创建 PdfWriter 和 PdfDocument 对象
//            try (PdfWriter writer = new PdfWriter(filePath);
//                 PdfDocument pdf = new PdfDocument(writer);
//                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(fontPath,
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
//                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
//                document.setFont(font);
//                // 创建段落
//                Paragraph paragraph = new Paragraph(content);
//                // 添加段落并关闭文档
//                document.add(paragraph);
//            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}