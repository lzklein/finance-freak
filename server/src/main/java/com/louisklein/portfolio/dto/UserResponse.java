package com.louisklein.portfolio.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String displayName;
    private OffsetDateTime createdAt;
}