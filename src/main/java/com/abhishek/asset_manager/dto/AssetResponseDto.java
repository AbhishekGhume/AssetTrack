package com.abhishek.asset_manager.dto;

import com.abhishek.asset_manager.model.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "All available assets")
public class AssetResponseDto {
    @Schema(description = "Asset id")
    private String assetId;

    @Schema(description = "Asset name")
    private String name;

    @Schema(description = "Quantity of the asset")
    private int quantity;

    @Schema(description = "Asset status")
    private AssetStatus status;

    @Schema(description = "Asset manager Id")
    private String assetManagerId;
}
