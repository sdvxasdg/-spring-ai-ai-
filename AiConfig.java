package com.atguigo.springaiproject_1.config;

import com.atguigo.springaiproject_1.service.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {


    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 VectorStoreService vectorStoreService,
                                 ChatMemory redisChatMemory) {
        return builder
                .defaultSystem("""
                        你是一名专业的招聘助手，会结合候选人简历、岗位要求和投递记录回答用户问题。
                        回答要专业、简洁，偶尔可以带点幽默感。
                        如果用户问的是对话历史中已经提到的信息（比如名字、之前说过的内容），请直接回答。
                        只有当问题确实与招聘无关，且对话历史中也没有相关信息时，才回答：我只是一个招聘助手，不能回答这个问题哦。
                        """)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()

                )
                .build();
    }
}