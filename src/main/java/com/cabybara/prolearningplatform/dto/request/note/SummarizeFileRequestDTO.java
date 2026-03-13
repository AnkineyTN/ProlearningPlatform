package com.cabybara.prolearningplatform.dto.request.note;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummarizeFileRequestDTO {
    @NotNull
    @JsonProperty("asset_id")
    @JsonAlias("assetId")
    private Long assetId;

    @NotBlank
    @JsonProperty("file_url")
    @JsonAlias("fileUrl")
    private String fileUrl;

    private String lang;

    private int limit;
}
