package com.cabybara.prolearningplatform.service.appeal;

import com.cabybara.prolearningplatform.dto.request.admin.AdminAppealReviewDto;
import com.cabybara.prolearningplatform.dto.response.appeal.AppealResponseDto;
import com.cabybara.prolearningplatform.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppealService {
    AppealResponseDto submitPublicAppeal(String email, String reason);
    Page<AppealResponseDto> listAppeals(AppealStatus status, Pageable pageable);
    AppealResponseDto reviewAppeal(Long appealId, AdminAppealReviewDto dto);
}
