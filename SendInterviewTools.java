package com.atguigo.springaiproject_1.tools;

import com.atguigo.springaiproject_1.entity.Interview;
import com.atguigo.springaiproject_1.tools.ToolLogger;
import com.atguigo.springaiproject_1.mapper.InterviewMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SendInterviewTools {

    private static final String TOOL_NAME = "sendInterview";
    private final InterviewMapper interviewMapper;

    public SendInterviewTools(InterviewMapper interviewMapper) {
        this.interviewMapper = interviewMapper;
    }

    public record SendInterviewResponse(boolean success, String message) {}

    @Tool(description = "为指定候选人安排面试，需要提供候选人ID、岗位ID和面试时间")
    public SendInterviewResponse sendInterview(
            @ToolParam(description = "候选人ID") Long candidateId,
            @ToolParam(description = "岗位ID") Long jobId,
            @ToolParam(description = "面试时间，ISO格式如 2026-09-25T14:00:00") String time) {

        long start = System.currentTimeMillis();
        ToolLogger.onEnter(TOOL_NAME,
                "candidateId=" + candidateId + ", jobId=" + jobId + ", time=" + time);

        // 参数校验
        if (candidateId == null || jobId == null || time == null || time.isBlank()) {
            ToolLogger.onInvaliArgs(TOOL_NAME,
                    "candidateId / jobId / time 有缺失");
            return new SendInterviewResponse(false,
                    "参数错误：候选人ID、岗位ID和面试时间都不能为空");
        }

        // 时间格式单独校验
        LocalDateTime interviewTime;
        try {
            interviewTime = LocalDateTime.parse(time);
        } catch (Exception e) {
            ToolLogger.onInvaliArgs(TOOL_NAME, "时间格式错误: " + time);
            return new SendInterviewResponse(false,
                    "时间格式错误，请用 ISO 格式（如 2026-09-25T14:00:00）");
        }

        try {
            Interview interview = new Interview();
            interview.setCandidateId(candidateId);
            interview.setJobId(jobId);
            interview.setInterviewTime(interviewTime);
            interview.setStatus("SCHEDULED");
            interviewMapper.insert(interview);

            SendInterviewResponse response = new SendInterviewResponse(true,
                    "面试已安排，ID=" + interview.getId());
            ToolLogger.onSuccess(TOOL_NAME, response, System.currentTimeMillis() - start);
            return response;

        } catch (Exception e) {
            ToolLogger.onError(TOOL_NAME, e);
            return new SendInterviewResponse(false, "系统繁忙，请稍后重试");
        }
    }
}