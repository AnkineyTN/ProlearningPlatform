package com.cabybara.prolearningplatform.dto.response.note;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteFileRegionCommentResponseDTO {
    private Long id;
    private Long noteId;
    private Long noteAssetId;
    private String kind;
    private int pageNumber;
    private RectPercentResponseDTO rectPercent;
    private String content;
    /** Optional image embedded in the comment (screenshot). */
    private Long attachmentAssetId;
    private String attachmentImageUrl;
    private String clientCommentId;
    /** ISO-8601 */
    private String createdAt;
    private String updatedAt;
}
