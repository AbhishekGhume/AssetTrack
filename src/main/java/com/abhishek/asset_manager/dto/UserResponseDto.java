package com.abhishek.asset_manager.dto;

import com.abhishek.asset_manager.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "All users information")
public class UserResponseDto {
    @Schema(description = "User id")
    private String id;

    @Schema(description = "Name of the user")
    private String name;

    @Schema(description = "Email of the user")
    private String email;

    @Schema(description = "Roles of the user")
    private Set<Role> role;
}
