package com.louisklein.portfolio.controller;

import com.louisklein.portfolio.dto.WatchlistRequest;
import com.louisklein.portfolio.dto.WatchlistResponse;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.model.Watchlist;
import com.louisklein.portfolio.model.WatchlistAsset;
import com.louisklein.portfolio.service.Result;
import com.louisklein.portfolio.service.UserService;
import com.louisklein.portfolio.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlists(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Watchlist> watchlists = watchlistService.findByUserId(user.getId());
        return ResponseEntity.ok(watchlists.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchlistResponse> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        Watchlist watchlist = watchlistService.findById(id);
        return ResponseEntity.ok(toResponse(watchlist));
    }

    @PostMapping
    public ResponseEntity<?> createWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WatchlistRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Watchlist> result = watchlistService.createWatchlist(user.getId(), request.getName());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result.getPayload()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> renameWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody WatchlistRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Watchlist> result = watchlistService.renameWatchlist(id, user.getId(), request.getName());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(toResponse(result.getPayload()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Void> result = watchlistService.deleteWatchlist(id, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assets")
    public ResponseEntity<?> addAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody UUID assetId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<WatchlistAsset> result = watchlistService.addAsset(id, assetId, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/assets/{assetId}")
    public ResponseEntity<?> removeAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Void> result = watchlistService.removeAsset(id, assetId, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    private WatchlistResponse toResponse(Watchlist watchlist) {
        WatchlistResponse response = new WatchlistResponse();
        response.setId(watchlist.getId());
        response.setName(watchlist.getName());
        response.setCreatedAt(watchlist.getCreatedAt());
        return response;
    }
}