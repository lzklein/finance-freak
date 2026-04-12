package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    List<Watchlist> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}