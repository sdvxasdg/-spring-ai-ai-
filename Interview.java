package com.atguigo.springaiproject_1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("interview")
public class Interview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateId;
    private Long jobId;
    private LocalDateTime interviewTime;
    private String interviewer;
    private String status;
    private LocalDateTime createTime;
}