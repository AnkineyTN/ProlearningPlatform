package com.cabybara.prolearningplatform.dto.request.admin;

import com.cabybara.prolearningplatform.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partial update payload for admin user management. Omit fields that should stay unchanged.")
public class AdminUserUpdateRequestDto {

    @Schema(description = "User's given name", example = "Jane")
    @Length(max = 100)
    private String firstName;

    @Schema(description = "User's family name", example = "Doe")
    @Length(max = 100)
    private String lastName;

    @Schema(description = "Subscription tier: FREE or PRO", example = "PRO")
    private AccountType accountType;
}
