package com.cabybara.prolearningplatform.dto.request.exam;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class GenerateExamByFileRequestDto {
    private List<MultipartFile> files;
    private Map<String, Integer> questions;
    private String language;
}
