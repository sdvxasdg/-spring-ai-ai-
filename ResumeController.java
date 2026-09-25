package com.atguigo.springaiproject_1.controller;

import com.atguigo.springaiproject_1.common.BusinessException;
import com.atguigo.springaiproject_1.common.Result;
import com.atguigo.springaiproject_1.entity.Candidate;
import com.atguigo.springaiproject_1.mapper.CandidateMapper;
import com.atguigo.springaiproject_1.service.FileParserService;
import com.atguigo.springaiproject_1.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final FileParserService fileParserService;
    private final VectorStoreService vectorStoreService;
    private final CandidateMapper candidateMapper;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) throws Exception {

        System.out.println(">>> [Upload] 收到文件: " + file.getOriginalFilename()
                + ", size=" + file.getSize() + " bytes, name=" + name);

        // ★ 参数校验：用 BusinessException 而不是直接 return
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("候选人姓名不能为空");
        }

        String text = fileParserService.parse(file);
        if (text == null || text.isBlank()) {
            throw new BusinessException("解析出的简历内容为空");
        }
        System.out.println(">>> [Upload] 解析出文本长度 = " + text.length());

        Candidate c = new Candidate();
        c.setName(name);
        c.setResumeText(text);
        c.setFilePath(file.getOriginalFilename());
        candidateMapper.insert(c);
        System.out.println(">>> [Upload] 候选人已入库, id=" + c.getId());

        vectorStoreService.addDocument(file.getOriginalFilename(), text);

        // ★ 返回统一格式
        return Result.success(Map.of(
                "candidateId", c.getId(),
                "name", name,
                "textLength", text.length()
        ));
    }
}