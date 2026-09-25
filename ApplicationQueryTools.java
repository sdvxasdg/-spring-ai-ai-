package com.atguigo.springaiproject_1.tools;

import com.atguigo.springaiproject_1.entity.ApplicationRecord;
import com.atguigo.springaiproject_1.entity.Candidate;
import com.atguigo.springaiproject_1.mapper.ApplicationRecordMapper;
import com.atguigo.springaiproject_1.mapper.CandidateMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationQueryTools {

    private static final String TOOL_NAME = "applicationQuery";
    private final CandidateMapper candidateMapper;
    private final ApplicationRecordMapper appMapper;

    public ApplicationQueryTools(CandidateMapper candidateMapper,
                                 ApplicationRecordMapper appMapper) {
        this.candidateMapper = candidateMapper;
        this.appMapper = appMapper;
    }

    public record ApplicationQueryResponse(String candidateName,
                                           List<String> jobs,
                                           List<String> status) {}

    @Tool(description = "根据候选人姓名查询其投递过的岗位及当前状态")
    public ApplicationQueryResponse queryApplication(
            @ToolParam(description = "候选人姓名") String candidateName) {

        long start = System.currentTimeMillis();
        ToolLogger.onEnter(TOOL_NAME, candidateName);

        // 参数校验
        if (candidateName == null || candidateName.isBlank()) {
            ToolLogger.onInvaliArgs(TOOL_NAME, "candidateName 为空");
            return new ApplicationQueryResponse(null, List.of(),
                    List.of("参数错误：候选人姓名不能为空"));
        }

        try {
            Candidate c = candidateMapper.selectOne(
                    new QueryWrapper<Candidate>()
                            .eq("name", candidateName)
                            .last("limit 1"));

            ApplicationQueryResponse response;
            if (c == null) {
                ToolLogger.onBusinessFail(TOOL_NAME, "未找到候选人: " + candidateName);
                response = new ApplicationQueryResponse(candidateName,
                        List.of(), List.of("未找到候选人"));
            } else {
                List<ApplicationRecord> list = appMapper.selectList(
                        new QueryWrapper<ApplicationRecord>()
                                .eq("candidate_id", c.getId()));
                response = new ApplicationQueryResponse(
                        c.getName(),
                        list.stream().map(a -> "jobId=" + a.getJobId()).toList(),
                        list.stream().map(ApplicationRecord::getStatus).toList()
                );
            }

            ToolLogger.onSuccess(TOOL_NAME, response, System.currentTimeMillis() - start);
            return response;

        } catch (Exception e) {
            ToolLogger.onError(TOOL_NAME, e);
            return new ApplicationQueryResponse(candidateName,
                    List.of(), List.of("系统繁忙，请稍后重试"));
        }
    }
}