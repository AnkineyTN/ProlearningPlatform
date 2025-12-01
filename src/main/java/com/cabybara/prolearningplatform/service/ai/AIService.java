package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.note.ConvertFileToVectorRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;

public interface AIService {
    public void convertFileToVector(ConvertFileToVectorRequestDTO request);

    public ExplainNoteResponseDTO explainNote(ExplainNoteRequestDTO request);

    public SummarizeFileResponseDTO summarizeFile(SummarizeFileRequestDTO request);
}
