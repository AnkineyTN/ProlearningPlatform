package com.cabybara.prolearningplatform.dto.request.pomodoro;

import com.cabybara.prolearningplatform.enums.AssetSource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSpaceSearchRequestDto {
     private String keyword;          

    private AssetSource source;      

    private SpaceTab tab = SpaceTab.ALL;

    @Min(0)
    private int page = 0;

    @Min(1) @Max(50)
    private int size = 20;

    private String sortBy = "createdAt";   // createdAt | name
    private String sortDir = "desc";       // asc | desc

    public enum SpaceTab { ALL, MY_UPLOADS, FAVORITES }
}
