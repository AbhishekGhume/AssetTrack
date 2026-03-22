package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.dto.AssetResponseDto;
import com.abhishek.asset_manager.exceptions.OutOfStock;
import com.abhishek.asset_manager.exceptions.UserNotExistsException;
import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.model.RequestStatus;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.AssetRepo;
import com.abhishek.asset_manager.repository.RequestRepo;
import com.abhishek.asset_manager.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AssetService {
    @Autowired
    private AssetRepo assetRepo;

    public List<AssetResponseDto> getAllAssets() {
        return assetRepo.findAll()
                .stream()
                .map(asset -> {
                    AssetResponseDto assetResponseDto = new AssetResponseDto();
                    assetResponseDto.setAssetId(asset.getId().toHexString());
                    assetResponseDto.setName(asset.getName());
                    assetResponseDto.setQuantity(asset.getQuantity());
                    assetResponseDto.setStatus(asset.getStatus());
                    assetResponseDto.setAssetManagerId(asset.getAssignedManagerId().toHexString());
                    assetResponseDto.setImageUrl(asset.getImageUrl());
                    return assetResponseDto;
                }).toList();
    }

}