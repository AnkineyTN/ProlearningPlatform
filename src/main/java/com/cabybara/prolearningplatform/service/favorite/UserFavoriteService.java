package com.cabybara.prolearningplatform.service.favorite;

import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserFavoriteService {
    
    /**
     * Toggles favorite status for a resource.
     *
     * @param userId       ID of the user.
     * @param resourceId   ID of the resource.
     * @param resourceType Type of the resource (NOTE, FLASHCARD, EXAM).
     * @return true if the resource is now favorited, false if it was unfavorited.
     */
    boolean toggleFavorite(Long userId, Long resourceId, ContentType resourceType);

    /**
     * Get paginated favorite resources of a user.
     *
     * @param userId       ID of the user.
     * @param resourceType Optional type of the resource to filter.
     * @param pageable     Pagination information.
     * @return Paginated list of favorited resources.
     */
    Page<SocialItemResponseDto> getFavoriteResources(Long userId, String q, ContentType resourceType, Pageable pageable);
}
