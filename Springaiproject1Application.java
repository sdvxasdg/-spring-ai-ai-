package com.atguigo.springaiproject_1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {PgVectorStoreAutoConfiguration.class})  // ★ 排除自动配置
@EnableCaching
@MapperScan("com.atguigo.springaiproject_1.mapper")
public class Springaiproject1Application {

    public static void main(String[] args) {
        SpringApplication.run(Springaiproject1Application.class, args);
    }

}
