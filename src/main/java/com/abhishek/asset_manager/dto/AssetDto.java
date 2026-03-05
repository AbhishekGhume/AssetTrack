package com.abhishek.asset_manager.dto;

import com.abhishek.asset_manager.model.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to add a new asset")
public class AssetDto {
    @NotBlank(message = "Name of asset is required")
    @Schema(example = "HMI board", description = "The name of the asset")
    private String name;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(example = "10", description = "The quantity of the asset")
    private int quantity;

    @Schema(example = "AVAILABLE", description = "The status of the asset. If you don't mention, by default it is 'AVAILABLE'")
    private AssetStatus status;

    @NotBlank(message = "Id of the asset manager of asset is required")
    @Schema(example = "69a1f41eb94387eec898a845", description = "The id of the asset manager")
    private String assignedManagerId;
}
