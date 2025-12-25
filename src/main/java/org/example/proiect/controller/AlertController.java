package org.example.proiect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.proiect.dto.AlertDto;
import org.example.proiect.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get recent alerts", description = "Returns alerts from the last N hours")
    public ResponseEntity<List<AlertDto>> getRecentAlerts(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(alertService.getRecentAlerts(hours));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread alerts", description = "Returns all unread alerts")
    public ResponseEntity<List<AlertDto>> getUnreadAlerts() {
        return ResponseEntity.ok(alertService.getUnreadAlerts());
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread alert count", description = "Returns the number of unread alerts")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", alertService.getUnreadCount()));
    }

    @GetMapping("/symbol/{ticker}")
    @Operation(summary = "Get alerts by symbol", description = "Returns alerts for a specific symbol")
    public ResponseEntity<List<AlertDto>> getAlertsBySymbol(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(alertService.getAlertsBySymbol(ticker, limit));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark alert as read", description = "Marks a specific alert as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Marks all alerts as read")
    public ResponseEntity<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}