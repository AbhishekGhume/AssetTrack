package com.abhishek.asset_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to login a user")
public class LoginRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in proper format")
    @Schema(example = "abhishek.ghume@gmail.com", description = "registered email address used for authentication")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "Abhishek@1234", description = "The user password same as that used for registration")
    private String password;
}
