package com.atguigo.springaiproject_1.tools;

import com.atguigo.springaiproject_1.entity.Candidate;
import com.atguigo.springaiproject_1.tools.ToolLogger;
import com.atguigo.springaiproject_1.mapper.CandidateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScreenCandidateTools {

    private static final String TOOL_NAME = "screenCandidate";
    private final CandidateMapper candidateMapper;

    public ScreenCandidateTools(CandidateMapper candidateMapper) {
        this.candidateMapper = candidateMapper;
    }

    public record ScreenCandidateResponse(List<Item> items) {}
    public record Item(Long id, String name, String summary) {}

    @Tool(description = "根据关键词筛选候选人简历，返回候选人列表")
    public ScreenCandidateResponse screenCandidate(
            @ToolParam(description = "筛选关键词，如技能、经历等") String keyword) {

        long start = System.currentTimeMillis();
        ToolLogger.onEnter(TOOL_NAME, keyword);

        // 参数校验
        if (keyword == null || keyword.isBlank()) {
            ToolLogger.onInvaliArgs(TOOL_NAME, "keyword 为空");
            return new ScreenCandidateResponse(List.of());
        }

        try {
            List<Candidate> list = candidateMapper.selectList(
                    new QueryWrapper<Candidate>()
                            .like("resume_text", keyword)
                            .last("limit 5"));

            List<Item> items = list.stream()
                    .map(c -> {
                        String text = c.getResumeText() == null ? "" : c.getResumeText();
                        String summary = text.substring(0, Math.min(80, text.length()));
                        return new Item(c.getId(), c.getName(), summary);
                    })
                    .toList();

            ScreenCandidateResponse response = new ScreenCandidateResponse(items);
            if (items.isEmpty()) {
                ToolLogger.onBusinessFail(TOOL_NAME, "无匹配候选人, keyword=" + keyword);
            }
            ToolLogger.onSuccess(TOOL_NAME, response, System.currentTimeMillis() - start);
            return response;

        } catch (Exception e) {
            ToolLogger.onError(TOOL_NAME, e);
            return new ScreenCandidateResponse(List.of());
        }
    }
}