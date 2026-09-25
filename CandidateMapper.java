package com.atguigo.springaiproject_1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.atguigo.springaiproject_1.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CandidateMapper extends BaseMapper<Candidate> {
    
}



