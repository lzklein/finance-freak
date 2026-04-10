package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, UUID> {

    List<AlertHistory> findByUserId(UUID userId);

    List<AlertHistory> findByAlertId(UUID alertId);

    List<AlertHistory> findByUserIdOrderByTriggeredAtDesc(UUID userId);
}