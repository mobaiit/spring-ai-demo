package com.ai.demo.rag.config;

import com.ai.demo.rag.utils.DocumentLoader;
import com.ai.demo.rag.utils.MyKeywordEnricher;
import com.ai.demo.rag.utils.MyTokenTextSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class VectorStoreConfig {

    @Resource
    private DocumentLoader documentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Value("${spring.ai.openai.api-key}")
    private String openAIApiKey;

    // 手动配置向量模型
    /*@Bean
    public EmbeddingModel openAiEmbeddingModel() {
        // 根据配置选择嵌入模型
        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-ada-002").build();
        OpenAiApi openaiApiKey = OpenAiApi.builder()
                .apiKey("demo")
                .baseUrl("demo")
                .embeddingsPath("demo")
                .build();
        return new OpenAiEmbeddingModel(openaiApiKey,
                MetadataMode.EMBED, embeddingOptions);
    }*/

    // 自动配置向量模型
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        initVectorStore(vectorStore);
        return vectorStore;
    }


    public void initVectorStore(SimpleVectorStore vectorStore) {
        Thread.startVirtualThread(()->{
            // 加载文档
            List<Document> documentList = documentLoader.loadMarkdowns();
            // 自主切分文档
            //List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
            // 自动补充关键词元信息
            List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documentList);
            vectorStore.add(enrichedDocuments);
        });
    }
}
