package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByUserId(UUID userId);

    List<Alert> findByUserIdAndActive(UUID userId, boolean active);

    List<Alert> findByAssetId(UUID assetId);

    List<Alert> findByActive(boolean active);

    boolean existsByUserIdAndAssetIdAndCondition(UUID userId, UUID assetId, Alert.AlertCondition condition);
}