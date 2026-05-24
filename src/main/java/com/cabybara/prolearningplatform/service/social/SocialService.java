package com.cabybara.prolearningplatform.service.social;

import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.TopCreatorResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.TopTopicResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.TrendingResourceResponseDto;
import com.cabybara.prolearningplatform.enums.ResourceType;
import com.cabybara.prolearningplatform.enums.TrendingPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SocialService {
    Page<SocialItemResponseDto> getSocialNotes(String q, Pageable pageable);
    Page<SocialItemResponseDto> getSocialFlashcards(String q, Pageable pageable);
    Page<SocialItemResponseDto> getSocialExams(String q, Pageable pageable);

    List<TrendingResourceResponseDto> getTrendingResources(TrendingPeriod period, int topN, ResourceType type);

    List<TopCreatorResponseDto> getTopCreators(TrendingPeriod period, int topN);

    List<TopTopicResponseDto> getTopTopics(TrendingPeriod period, int topN);
}

