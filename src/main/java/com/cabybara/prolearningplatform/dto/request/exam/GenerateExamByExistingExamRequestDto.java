package com.cabybara.prolearningplatform.dto.request.exam;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
public class GenerateExamByExistingExamRequestDto {
    private List<MultipartFile> files;
}
