package com.atguigo.springaiproject_1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("application_record")
public class ApplicationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long candidateId;
    private Long jobId;
    private String status;
    private LocalDateTime createTime;
}