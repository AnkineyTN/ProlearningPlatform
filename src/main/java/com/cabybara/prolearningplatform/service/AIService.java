package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.request.ConvertFileToVectorRequestDTO;

public interface AIService {
    public void convertFileToVector(ConvertFileToVectorRequestDTO request);
}
