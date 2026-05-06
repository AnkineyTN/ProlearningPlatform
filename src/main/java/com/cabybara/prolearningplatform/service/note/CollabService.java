package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.response.share.VerifyAccessResponse;
import com.cabybara.prolearningplatform.dto.response.share.YjsStateResponse;

public interface CollabService {
    VerifyAccessResponse verifyAccess(Long userId, Long noteId);

    YjsStateResponse getYjsState(Long noteId);

    void saveYjsState(Long noteId, String base64State);
}
