package com.ai.demo.rag.utils;


import org.springframework.stereotype.Component;

/**
 * 查询重写器
 */
@Component
public class QueryRewriter {

    //private final QueryTransformer queryTransformer;
    //
    //public QueryRewriter(ChatModel dashscopeChatModel) {
    //    ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
    //    // 创建查询重写转换器
    //    queryTransformer = RewriteQueryTransformer.builder()
    //            .chatClientBuilder(builder)
    //            .build();
    //}
    //
    ///**
    // * 执行查询重写
    // *
    // * @param prompt
    // * @return
    // */
    //public String doQueryRewrite(String prompt) {
    //    Query query = new Query(prompt);
    //    // 执行查询重写
    //    Query transformedQuery = queryTransformer.transform(query);
    //    // 输出重写后的查询
    //    return transformedQuery.text();
    //}
}
