package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.request.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.response.ExplainNoteResponseDTO;

public interface AIService {
    public void convertFileToVector(ConvertFileToVectorRequestDTO request);

    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request);
}
