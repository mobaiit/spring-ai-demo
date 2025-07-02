package com.ai.demo.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.ai.demo.common.constant.FileConstant;
import com.ai.demo.common.utils.FontUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI友好型PDF生成工具
 * 
 * 设计说明：
 * 1. 使用简单的Map结构表示内容，方便AI生成
 * 2. 支持多种内容类型：文本(text)、图片(image)、标题(heading)
 * 3. 图片支持URL和Base64两种格式
 * 4. 自动处理排版和格式
 * 
 * 内容格式示例：
 * [
 *   {"type": "heading", "level": 1, "content": "主标题"},
 *   {"type": "text", "content": "这是第一段文本..."},
 *   {"type": "image", "source": "https://example.com/image.jpg"},
 *   {"type": "heading", "level": 2, "content": "二级标题"},
 *   {"type": "text", "content": "这是第二段文本..."}
 * ]
 */
public class PDFGenerationTool {
    
    // 支持的内容类型
    private static final Set<String> VALID_TYPES = 
        Set.of("text", "image", "heading", "divider");

    /**
     * 生成PDF文档 (AI友好接口)
     * 
     * @param fileName PDF文件名（不需要扩展名）
     * @param title 文档标题
     * @param contentList 内容列表（每个元素是一个Map，包含type和content/source）
     * @return 生成结果信息
     */
    @Tool(description = "Generate a PDF document with mixed text and images. "
            + "Content should be provided as a list of maps with keys: "
            + "'type' (text/image/heading/divider), "
            + "for text: 'content' (text string), "
            + "for image: 'source' (URL or base64 data), "
            + "for heading: 'level' (1-6) and 'content' (heading text). "
            + "Example: [{'type':'heading','level':1,'content':'Title'}, "
            + "{'type':'text','content':'Paragraph text'}, "
            + "{'type':'image','source':'data:image/png;base64,...'}]",
            returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the PDF file (without .pdf extension)") String fileName,
            @ToolParam(description = "Title of the document") String title,
            @ToolParam(description = "List of content blocks") List<Map<String, Object>> contentList) {
        
        // 验证输入
        if (StrUtil.isBlank(fileName)) {
            return "Error: File name cannot be empty";
        }
        if (StrUtil.isBlank(title)) {
            return "Error: Document title cannot be empty";
        }
        if (contentList == null || contentList.isEmpty()) {
            return "Error: PDF must contain at least one content block";
        }
        
        // 添加文件扩展名
        fileName = fileName + ".pdf";
        
        // 创建保存目录
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        FileUtil.mkdir(fileDir);
        
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 设置字体
            PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
            document.setFont(font);

            // 添加文档标题
            addTitle(document, title);
            
            // 添加内容块
            for (Map<String, Object> block : contentList) {
                String type = (String) block.getOrDefault("type", "text");
                
                if (!VALID_TYPES.contains(type)) {
                    continue; // 跳过无效类型
                }
                
                switch (type) {
                    case "text":
                        addTextBlock(document, (String) block.get("content"));
                        break;
                    case "image":
                        addImageBlock(document, (String) block.get("source"));
                        break;
                    case "heading":
                        int level = (int) block.getOrDefault("level", 2);
                        addHeading(document, level, (String) block.get("content"));
                        break;
                    case "divider":
                        addDivider(document);
                        break;
                }
            }
            
            return "PDF文档生成成功！文件路径: " + filePath;
        } catch (IOException e) {
            return "生成PDF时出错: " + e.getMessage();
        } catch (Exception e) {
            return "处理内容时出错: " + e.getMessage();
        }
    }

    // 添加文档标题
    private void addTitle(Document document, String title) {
        try {
            Paragraph titlePara = new Paragraph(title)
                    .setFont(FontUtils.getBoldFont())
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titlePara);
        } catch (Exception e) {
            // 字体加载失败时使用普通字体
            addTextBlock(document, title);
        }
    }
    
    // 添加文本块
    private void addTextBlock(Document document, String text) {
        if (StrUtil.isBlank(text)) return;
        
        Paragraph paragraph = new Paragraph(text)
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(15);
        document.add(paragraph);
    }
    
    // 添加图片块
    private void addImageBlock(Document document, String source) {
        if (StrUtil.isBlank(source)) return;
        
        try {
            Image image = createImageFromSource(source);
            // 设置图片大小（最大宽度为页面宽度80%）
            float pageWidth = document.getPageEffectiveArea(document.getPdfDocument().getDefaultPageSize()).getWidth();
            float maxWidth = pageWidth * 0.8f;

            if (image.getImageWidth() > maxWidth) {
                float ratio = maxWidth / image.getImageWidth();
                image.scale(ratio, ratio);
            }
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMarginTop(10);
            image.setMarginBottom(15);
            document.add(image);
        } catch (Exception e) {
            // 图片处理错误，添加错误信息
            addTextBlock(document, "图片地址: [" + source + "]");
        }
    }

    // 添加标题
    private void addHeading(Document document, int level, String text) {
        if (StrUtil.isBlank(text)) return;

        float[] sizes = {24, 20, 18, 16, 14, 12}; // H1到H6的大小
        float size = sizes[Math.min(level - 1, sizes.length - 1)];

        try {
            Paragraph heading = new Paragraph(text)
                    .setFont(level <= 2 ? FontUtils.getBoldFont() : FontUtils.getNormalFont())
                    .setFontSize(size)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(heading);
        } catch (Exception e) {
            // 字体加载失败时使用普通样式
            Paragraph heading = new Paragraph(text)
                    .setFontSize(size)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(heading);
        }
    }
    
    // 添加分隔线
    private void addDivider(Document document) {
        Div divider = new Div();
        divider.setBorder(new SolidBorder(0.5f));
        divider.setMarginTop(15);
        divider.setMarginBottom(15);
        document.add(divider);
    }

    // 增强版图片加载方法
    private Image createImageFromSource(String source) throws IOException {
        ImageData imageData;
        // 1. 处理Base64编码的图片
        if (source.startsWith("data:image")) {
            String base64Data = source.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            imageData = ImageDataFactory.create(imageBytes);

        }
        // 2. 处理URL - 使用自定义下载器避免证书问题
        else if (source.startsWith("http")) {
            try {
                // 使用Hutool下载图片
                byte[] imageBytes = HttpUtil.downloadBytes(source);
                imageData = ImageDataFactory.create(imageBytes);
            } catch (Exception e) {
                throw new IOException("下载图片失败: " + e.getMessage());
            }
        }
        // 3. 处理本地文件
        else {
            Path path = Paths.get(source);
            if (!Files.exists(path)) {
                throw new IOException("图片文件不存在: " + source);
            }
            imageData = ImageDataFactory.create(path.toAbsolutePath().toString());
        }
        return new Image(imageData);
    }
}