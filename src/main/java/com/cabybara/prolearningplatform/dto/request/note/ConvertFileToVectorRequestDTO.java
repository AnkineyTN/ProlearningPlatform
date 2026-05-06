package com.cabybara.prolearningplatform.dto.request.note;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConvertFileToVectorRequestDTO {
    @NotNull
    @JsonProperty("note_id")
    @JsonAlias("noteId")
    private Long noteId;

    @NotNull
    @JsonProperty("asset_id")
    @JsonAlias("assetId")
    private Long assetId;

    @NotBlank
    @JsonProperty("file_name")
    @JsonAlias("fileName")
    private String fileName;

    @NotBlank
    @JsonProperty("file_url")
    @JsonAlias("fileUrl")
    private String fileUrl;
}
