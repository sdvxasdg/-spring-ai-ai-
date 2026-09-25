package com.atguigo.springaiproject_1.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PgVectorConfig {

    /**
     * ★ 关键改动：不注册 DataSource Bean，只在方法内部创建。
     * 这样 Spring Boot 的 spring.sql.init 就不会把 schema.sql 往 PG 上跑。
     */
    @Bean
    public JdbcTemplate pgVectorJdbcTemplate(
            @Value("${spring.pgvector.datasource.url}") String url,
            @Value("${spring.pgvector.datasource.username}") String username,
            @Value("${spring.pgvector.datasource.password}") String password,
            @Value("${spring.pgvector.datasource.driver-class-name}") String driverClassName) {

        DataSourceProperties props = new DataSourceProperties();
        props.setUrl(url);
        props.setUsername(username);
        props.setPassword(password);
        props.setDriverClassName(driverClassName);

        // ★ 用 HikariDataSource 明确配置连接池
        HikariDataSource dataSource = props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(5000);
        dataSource.setPoolName("pgvector-pool");

        return new JdbcTemplate(dataSource);
    }
    @Bean
    public VectorStore vectorStore(JdbcTemplate pgVectorJdbcTemplate,
                                   EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(pgVectorJdbcTemplate, embeddingModel)
                .dimensions(1024)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .initializeSchema(true)
                .build();
    }
}