package com.abhishek.asset_manager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "requests")
public class Request {
    @Id
    private ObjectId id;
    @NonNull
    private ObjectId assetId;
    private RequestStatus requestStatus;
    @CreatedDate
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private LocalDateTime returnDate;
}
