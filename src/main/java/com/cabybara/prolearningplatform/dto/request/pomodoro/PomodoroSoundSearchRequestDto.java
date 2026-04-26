package com.cabybara.prolearningplatform.dto.request.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetSource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PomodoroSoundSearchRequestDto {
    private String keyword;
    private AssetSource source;
    private SoundTab tab = SoundTab.ALL;

    @Min(0) private int page = 0;
    @Min(1) @Max(50) private int size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "desc";

    public enum SoundTab { ALL, MY_UPLOADS, FAVORITES }
}
