package com.abhishek.asset_manager.dto;

import com.abhishek.asset_manager.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to register a new user")
public class UserRegistrationDto {
    @NotBlank(message = "Name is required")
    @Schema(example = "Abhishek Ghume", description = "The full name of the user")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in proper format")
    @Schema(example = "abhishek.ghume@gmail.com", description = "Unique email address used for authentication")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Requires minimum 6 characters")
    @Schema(example = "Abhishek@1234", description = "The user password (minimum 6 characters)")
    private String password;

    @Schema(example = "['EMPLOYEE']", description = "Role of the user")
    private Set<Role> role;
}
