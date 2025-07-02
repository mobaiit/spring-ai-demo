package com.ai.demo.common.utils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import java.io.IOException;

public class FontUtils {
    private static PdfFont normalFont;
    private static PdfFont boldFont;
    
    static {
        try {
            // 初始化中文字体（正常和粗体）
            normalFont = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
            boldFont = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
        } catch (IOException e) {
            try {
                // 回退到英文字体
                normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (IOException ex) {
                throw new RuntimeException("无法初始化字体", ex);
            }
        }
    }
    
    public static PdfFont getNormalFont() {
        return normalFont;
    }
    
    public static PdfFont getBoldFont() {
        return boldFont;
    }
}