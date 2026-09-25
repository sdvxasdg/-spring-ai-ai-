package com.atguigo.springaiproject_1.controller;

import com.atguigo.springaiproject_1.common.Result;
import com.atguigo.springaiproject_1.service.VectorStoreService;
import com.atguigo.springaiproject_1.tools.ApplicationQueryTools;
import com.atguigo.springaiproject_1.tools.JobQueryTools;
import com.atguigo.springaiproject_1.tools.ScreenCandidateTools;
import com.atguigo.springaiproject_1.tools.SendInterviewTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private static final String NOT_FOUND = "抱歉，我的知识库中没有找到相关信息，无法回答这个问题。";

    private static final double SIMILARITY_THRESHOLD = 0.3;  // ★ 相似度阈值
    private static final int TOP_K = 5;                        // ★ 最多返回 5 条

    private final ChatClient chatClient;
    private final ChatMemory redisChatMemory;
    private final VectorStore vectorStore;
    private final VectorStoreService vectorStoreService;

    // ★ 新增：4 个工具类注入
    private final JobQueryTools jobQueryTools;
    private final ApplicationQueryTools applicationQueryTools;
    private final ScreenCandidateTools screenCandidateTools;
    private final SendInterviewTools sendInterviewTools;

    @Autowired
    public ChatController(ChatClient chatClient,
                          ChatMemory redisChatMemory,
                          VectorStore vectorStore,
                          VectorStoreService vectorStoreService,
                          JobQueryTools jobQueryTools,
                          ApplicationQueryTools applicationQueryTools,
                          ScreenCandidateTools screenCandidateTools,
                          SendInterviewTools sendInterviewTools) {
        this.chatClient = chatClient;
        this.redisChatMemory = redisChatMemory;
        this.vectorStore = vectorStore;
        this.vectorStoreService = vectorStoreService;
        this.jobQueryTools = jobQueryTools;
        this.applicationQueryTools = applicationQueryTools;
        this.screenCandidateTools = screenCandidateTools;
        this.sendInterviewTools = sendInterviewTools;

        // 启动时断言：必须是同一个对象
        System.out.println(">>> ChatController.vectorStore  hashCode = "
                + System.identityHashCode(this.vectorStore));
        System.out.println(">>> VectorStoreService 内的     hashCode = "
                + System.identityHashCode(vectorStoreService.getVectorStore()));
        if (this.vectorStore != vectorStoreService.getVectorStore()) {
            throw new IllegalStateException(" VectorStore 不是单例！有两个实例！");
        }
        System.out.println(">>>  VectorStore 是同一个实例");
    }

    /**
     * 手动 RAG：检索为空返回 null（调用方据此短路）
     */
    private String buildRagSystemPrompt(String userMsg) {
        // ★ 使用 SearchRequest 构建检索请求，带阈值和 topK
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userMsg)
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        System.out.println(">>> [RAG] query='" + userMsg + "', 命中 " + docs.size() + " 条");

        if (docs.isEmpty()) {
            System.out.println(">>> [RAG] 命中 0 条 → 短路返回 null");
            return null;
        }

        // ★ 拼接 context 时带上来源文件名
        String context = docs.stream()
                .map(doc -> {
                    String filename = (String) doc.getMetadata().getOrDefault("filename", "未知来源");
                    return "【来源: " + filename + "】\n" + doc.getText();
                })
                .collect(Collectors.joining("\n---\n"));

        System.out.println(">>> [RAG] 参考资料: "
                + context.substring(0, Math.min(120, context.length())));

        return """
        你是一名专业的招聘助手。

        【可用工具】
        - jobQuery：根据岗位名称查询岗位要求
        - applicationQuery：根据候选人姓名查询投递记录和状态
        - screenCandidate：根据关键词筛选候选人
        - sendInterview：安排面试

        【回答规则】
        1. 优先使用【参考资料】回答；资料不足时调用对应工具。
        2. 参考资料中明确标注了来源文件（如【来源: 张三简历.txt】），回答时可以提及。
        3. 绝对禁止编造、猜测。
        4. 资料和工具都查不到时，回答"抱歉，没有找到相关信息"。

        【参考资料】
        %s
        """.formatted(context);
    }
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam("msg") String msg,
                               @RequestParam(defaultValue = "default") String conversationId) {
        System.out.println(">>> Controller 接收到的 conversationId = " + conversationId);

        String systemPrompt = buildRagSystemPrompt(msg);

        // 检索为空，直接返回，不调模型
        if (systemPrompt == null) {
            redisChatMemory.add(conversationId,
                    List.of(new UserMessage(msg), new AssistantMessage(NOT_FOUND)));
            return Result.success(NOT_FOUND);
        }

        List<Message> history = redisChatMemory.get(conversationId);
        List<Message> messages = new ArrayList<>(history);
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(msg));

        String answer = chatClient.prompt()
                .messages(messages)
                // ★ 1.1.2.0：.functions(...) 改为 .tools(...)，传入工具类实例
                .tools(jobQueryTools, applicationQueryTools, screenCandidateTools, sendInterviewTools)
                .call()
                .content();

        redisChatMemory.add(conversationId,
                List.of(new UserMessage(msg), new AssistantMessage(answer)));

        return Result.success(answer);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam("msg") String msg,
            @RequestParam(defaultValue = "default") String conversationId) {

        String systemPrompt = buildRagSystemPrompt(msg);

        // 检索为空：直接返回，不调模型
        if (systemPrompt == null) {
            redisChatMemory.add(conversationId,
                    List.of(new UserMessage(msg), new AssistantMessage(NOT_FOUND)));
            return Flux.just(
                    sse("message", NOT_FOUND),
                    sse("done", "[DONE]")
            );
        }

        List<Message> history = redisChatMemory.get(conversationId);
        List<Message> messages = new ArrayList<>(history);
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(msg));

        StringBuilder answerBuffer = new StringBuilder();

        // 内容流：每个 chunk 包装成一个 message 事件
        Flux<ServerSentEvent<String>> contentFlux = chatClient.prompt()
                .messages(messages)
                // ★ 1.1.2.0：同样改为 .tools(...)
                .tools(jobQueryTools, applicationQueryTools, screenCandidateTools, sendInterviewTools)
                .stream()
                .content()
                .doOnNext(answerBuffer::append)
                .map(chunk -> sse("message", chunk));

        // 内容结束后：写记忆 + 发 done 事件
        Flux<ServerSentEvent<String>> withDone = contentFlux.concatWith(
                Flux.defer(() -> {
                    if (answerBuffer.length() > 0) {
                        redisChatMemory.add(conversationId, List.of(
                                new UserMessage(msg),
                                new AssistantMessage(answerBuffer.toString())
                        ));
                        System.out.println(">>> [STREAM] 完成, 总长度=" + answerBuffer.length());
                    }
                    return Flux.just(sse("done", "[DONE]"));
                })
        );

        // 心跳：每 15 秒发一个 ping，避免连接超时断开
        Flux<ServerSentEvent<String>> heartbeat = Flux
                .interval(Duration.ofSeconds(15))
                .map(i -> sse("ping", ""));

        // 错误处理：把异常转成 error 事件
        Flux<ServerSentEvent<String>> safeFlux = withDone
                .onErrorResume(e -> {
                    System.err.println(">>> [STREAM] 出错: " + e.getMessage());
                    return Flux.just(sse("error", "服务出错: " + e.getMessage()));
                });

        // 合并内容流 + 心跳流；收到 done/error 时终止（否则心跳不会停）
        return Flux.merge(safeFlux, heartbeat)
                .takeUntil(ev -> "done".equals(ev.event()) || "error".equals(ev.event()))
                .doOnCancel(() -> System.out.println(">>> [STREAM] 客户端断开"));
    }

    /** 构造一个 SSE 事件的辅助方法 */
    private ServerSentEvent<String> sse(String event, String data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();
    }
}