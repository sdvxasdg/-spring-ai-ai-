package com.atguigo.springaiproject_1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorStoreService implements CommandLineRunner {

    private static final String DEMO_RESUME = """
            教育经历
            北京大学 软件工程 本科 2005.09 - 2009.06
            工作经验
            阿里巴巴有限公司 算法工程师 2009-07 - 2015-07
            小米科技有限公司 算法工程师 2015-08 - 2020-03
            熟悉Java、C++、Python，精通深度学习、目标检测、推荐系统。
            """;

    private static final String DEMO_FILENAME = "张三简历.txt";

    private final VectorStore vectorStore;

    public VectorStore getVectorStore() {
        return vectorStore;
    }

    /** ★ 应用启动后执行一次：确保 PgVector 里有张三的示例简历 */
    @Override
    public void run(String... args) {
        try {
            SearchRequest check = SearchRequest.builder()
                    .query("张三")
                    .topK(1)
                    .similarityThreshold(0.0)
                    .build();
            List<Document> existing = vectorStore.similaritySearch(check);

            boolean hasDemo = existing.stream()
                    .anyMatch(d -> DEMO_FILENAME.equals(d.getMetadata().get("filename")));

            if (hasDemo) {
                System.out.println(">>> [VectorStore] 已存在示例简历 " + DEMO_FILENAME + "，跳过初始化");
            } else {
                addDocument(DEMO_FILENAME, DEMO_RESUME);
                System.out.println(">>> [VectorStore] 示例简历 " + DEMO_FILENAME + " 已写入 PgVector");
            }
        } catch (Exception e) {
            System.err.println(">>> [VectorStore] 初始化示例简历失败: " + e.getMessage());
        }
    }

    public void addDocument(String filename, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("简历内容为空: " + filename);
        }

        ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        TextReader reader = new TextReader(resource);
        reader.getCustomMetadata().put("filename", filename);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter(1200, 350, 5, 100, true);
        List<Document> chunks = splitter.apply(documents);

        for (Document d : chunks) {
            d.getMetadata().put("filename", filename);
        }

        vectorStore.add(chunks);
        System.out.println(">>> [VectorStore] 已写入 " + chunks.size()
                + " 个 chunk 到 PgVector, filename=" + filename);
    }
}