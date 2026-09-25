package com.atguigo.springaiproject_1.config;

import com.atguigo.springaiproject_1.funtion.configfuntion;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class config {
    @Bean
    @Description("某某是否有资格面试")
    public Function<configfuntion.Request, configfuntion.Position> function() {
        return new configfuntion();
    }
    // ★ 删除了 @Bean VectorStore，统一由 VectorStoreService 管理，避免两个实例
//    @Bean
//    VectorStore vectorStore(EmbeddingModel embeddingModel) {
//        SimpleVectorStore simpleVectorStore=SimpleVectorStore.builder(embeddingModel).build();
//        String filename="张三简历.txt";
//        TextReader textReader=new TextReader(filename);
//        textReader.getCustomMetadata().put("filename",filename);
//        List<Document> documents=textReader.get();
//        TokenTextSplitter splitter =
//                new TokenTextSplitter(1200,
//                        350, 5,
//                        100, true);
//        splitter.apply(documents);
//        simpleVectorStore.add(documents);
//        return simpleVectorStore;
//
//
//    }
}
