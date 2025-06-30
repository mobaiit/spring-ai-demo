package com.mcp.server.tools;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageSearchTool {

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "WOBi0qVkSvX8IbqIcQICffKlTksBAEHJhO78jvYJmJBYcv9jiMu83BWK";

    // Pexels 常规搜索接口（请以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数（仅包含query，可根据文档补充page、per_page等参数）
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        // large(24MP), medium(12MP) or small(4MP).
        params.put("size", "medium");
        params.put("page", RandomUtil.randomInt(1000));
        params.put("per_page", 1);
        // 'en' 'zh-CN' 'es'
        params.put("locale", "zh-CN");
        log.info("search medium images query:{}", params);
        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();
        //log.info("search medium images response:{}", response);
        // 解析响应JSON（假设响应结构包含"photos"数组，每个元素包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> strings = new ImageSearchTool().searchMediumImages("猫");
        strings.forEach(System.out::println);
    }
}
