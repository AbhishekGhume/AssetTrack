package com.abhishek.asset_manager.dto;

import com.abhishek.asset_manager.model.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response of the asset request")
public class RequestResponseDto {
    @Schema(description = "Request id")
    private String requestId;

    @Schema(description = "Asset id")
    private String assetId;

    @Schema(description = "Request status")
    private RequestStatus requestStatus;

    @Schema(description = "Request date")
    private String requestDate;

    @Schema(description = "Approved date")
    private String approvedDate;

    @Schema(description = "Returned date")
    private String returnDate;
}
