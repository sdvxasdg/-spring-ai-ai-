package com.atguigo.springaiproject_1.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.atguigo.springaiproject_1.entity.Job;
import com.atguigo.springaiproject_1.mapper.JobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobQueryTools {
    private static final String TOOL_NAME = "jobQuery";
    private final JobMapper jobMapper;
    public JobQueryTools(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }
    public record JobQueryResponse(Long id, String title, String requirements) {}
    @Tool(description = "根据岗位名称查询岗位的详细要求和描述")
    @Cacheable(value = "job", key = "#title")          // ★ 新增
    public JobQueryResponse queryJob(@ToolParam(description = "根据岗位名称查询岗位的详细要求和描述")String title){
//        req 是输入：调用方传进来的请求，比如岗位名称。

            long start=System.currentTimeMillis();
//            onEnter 已经在进入方法时记录了 req
            ToolLogger.onEnter(TOOL_NAME,title);
            //参数校验
            if (title == null || title.isBlank()) {
            ToolLogger.onInvaliArgs(TOOL_NAME,title);
            return new JobQueryResponse(null,null, "参数错误：岗位名称不能为空");
        }
        //
            try {
                Job job=jobMapper.selectOne(new QueryWrapper<Job>().like("title",title)
                        .last("limit 1"));
                JobQueryResponse response;
                if(job==null){
                    ToolLogger.onBusinessFail(TOOL_NAME,title);
                    //业务失败
                    response=new JobQueryResponse(null,title,"没有找到相应岗位");
                }else {
                    response=new JobQueryResponse(job.getId(),job.getTitle(),job.getRequirements());
                }
                //成功日志
                 ToolLogger.onSuccess(TOOL_NAME,response,System.currentTimeMillis()-start);
                 return response;
            }
            catch (Exception e) {
                //异常处理
                ToolLogger.onError(TOOL_NAME,e);
                return new JobQueryResponse(null,title,"系统繁忙，请稍后再试");
            }

    }













//    public record Request(String title) {}
//    public record Response(Long id, String title, String requirements) {}
//
//    @Bean
//    @Description("根据岗位名称查询岗位的详细要求和描述")
//    public Function<Request, Response> jobQuery(JobMapper jobMapper) {
//        return req -> {
//            Job job = jobMapper.selectOne(
//                    new QueryWrapper<Job>().like("title", req.title()).last("limit 1"));
//            if (job == null) return new Response(null, req.title(), "未找到该岗位");
//            return new Response(job.getId(), job.getTitle(), job.getRequirements());
//        };
//    }
}