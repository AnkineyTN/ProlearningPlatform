package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.SummarizeFileResponseDTO;

public interface AIService {
    public void convertFileToVector(ConvertFileToVectorRequestDTO request);

    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request);

    public SummarizeFileResponseDTO summarizeFile(SummarizeFileRequestDTO request);
}
