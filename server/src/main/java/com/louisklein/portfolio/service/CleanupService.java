package com.louisklein.portfolio.service;

import com.louisklein.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final UserRepository userRepository;

    @Scheduled(fixedRate = 3600000) // runs every hour
    public void deleteUnverifiedUsers() {
        log.info("Running unverified user cleanup...");
        int deleted = userRepository.deleteExpiredUnverifiedUsers(OffsetDateTime.now());
        log.info("Deleted {} unverified users", deleted);
    }
}