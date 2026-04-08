package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNoteFileRegionCommentRequestDTO {

    /** Same as frontend `noteAssetId` (asset PK). */
    @NotNull
    @Min(1)
    private Long noteAssetId;

    /** `doc` or `image` — matches frontend `kind`. */
    @NotBlank
    @Pattern(regexp = "doc|image", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String kind;

    @NotNull
    @Min(1)
    private Integer pageNumber;

    @NotNull
    @Valid
    private RectPercentRequestDTO rectPercent;

    @NotBlank
    private String content;

    private String clientCommentId;

    /** If set, must match the asset's public id (Cloudinary). */
    private String publicId;
}
