package com.ai.demo.tools;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.charset.StandardCharsets;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        String result = "";
        try {
            if (StringUtils.isBlank(url) || !url.startsWith("http")) {
                return result;
            }
            Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (compatible; ResearchBot/1.0)")
                    .referrer("https://www.searchapi.io")
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .get()
                    .outputSettings(new Document.OutputSettings()
                            .charset(StandardCharsets.UTF_8));
            if (!doc.getElementsByTag("main").isEmpty()) {
                result = doc.getElementsByTag("main").get(0).html();
            } else {
                result = doc.body().html();
            }
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
        Cleaner cleaner = new Cleaner(Safelist.none());
        return cleaner.clean(Jsoup.parse(result)).text();
    }
}
