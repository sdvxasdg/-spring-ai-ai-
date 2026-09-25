package com.atguigo.springaiproject_1.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class RedisChatMemory implements ChatMemory {

    private static final String PREFIX = "chat:memory:";
    private static final int MAX_MESSAGES = 20;
    private static final long TTL_SECONDS = Duration.ofHours(24).getSeconds();

    private static final RedisScript<Long> ADD_SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local maxLen = tonumber(ARGV[1])
            local ttl = tonumber(ARGV[2])
            for i = 3, #ARGV do
                redis.call('RPUSH', key, ARGV[i])
            end
            redis.call('LTRIM', key, -maxLen, -1)
            redis.call('EXPIRE', key, ttl)
            return redis.call('LLEN', key)
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisChatMemory(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;

        List<String> args = new ArrayList<>(messages.size() + 2);
        args.add(String.valueOf(MAX_MESSAGES));
        args.add(String.valueOf(TTL_SECONDS));
        for (Message m : messages) {
            args.add(serialize(m));
        }

        String key = PREFIX + conversationId;
        Long len = redisTemplate.execute(ADD_SCRIPT, List.of(key), args.toArray());
        System.out.println(">>> [Memory] RPUSH " + messages.size()
                + " 条 → key=" + key + ", 当前长度=" + len);
    }

    /**
     * ★ 1.1.2.0 新签名：只接 conversationId。
     * 因为 add() 里已经用 LTRIM 保证列表长度 ≤ MAX_MESSAGES，这里直接读全量即可。
     */
    @Override
    public List<Message> get(String conversationId) {
        String key = PREFIX + conversationId;
        List<String> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) return new ArrayList<>();

        List<Message> result = new ArrayList<>(raw.size());
        for (String s : raw) {
            Message m = deserialize(s);
            if (m != null) result.add(m);
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(PREFIX + conversationId);
    }

    // ---------- 序列化 / 反序列化 ----------

    private String serialize(Message m) {
        try {
            return objectMapper.writeValueAsString(
                    new MessageDto(m.getMessageType().name(),
                            m.getText() == null ? "" : m.getText()));
        } catch (Exception e) {
            throw new RuntimeException("序列化消息失败", e);
        }
    }

    private Message deserialize(String s) {
        try {
            MessageDto dto = objectMapper.readValue(s, MessageDto.class);
            return switch (dto.type()) {
                case "USER"      -> new UserMessage(dto.text());
                case "ASSISTANT" -> new AssistantMessage(dto.text());
                case "SYSTEM"    -> new SystemMessage(dto.text());
                default          -> null;
            };
        } catch (Exception e) {
            System.err.println(">>> [Memory] 反序列化失败: " + s + ", err=" + e.getMessage());
            return null;
        }
    }

    public record MessageDto(String type, String text) {}
}