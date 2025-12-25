package org.example.proiect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.AlertDto;
import org.example.proiect.model.Alert;
import org.example.proiect.model.Symbol;
import org.example.proiect.repository.AlertRepository;
import org.example.proiect.repository.SymbolRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final SymbolRepository symbolRepository;
    private final WebSocketService webSocketService;

    public List<AlertDto> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertRepository.findRecentAlerts(since).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AlertDto> getUnreadAlerts() {
        return alertRepository.findByIsReadFalseOrderByTimestampDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AlertDto> getAlertsBySymbol(String ticker, int limit) {
        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));

        return alertRepository.findBySymbolOrderByTimestampDesc(symbol, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AlertDto createAlert(Symbol symbol, Alert.AlertType alertType, String message,
                                BigDecimal triggerValue, BigDecimal thresholdValue) {
        Alert alert = Alert.builder()
                .symbol(symbol)
                .alertType(alertType)
                .message(message)
                .triggerValue(triggerValue)
                .thresholdValue(thresholdValue)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        alert = alertRepository.save(alert);
        log.info("Created alert for {}: {} - {}", symbol.getTicker(), alertType, message);

        AlertDto alertDto = toDto(alert);

        // Broadcast alert via WebSocket
        webSocketService.broadcastAlert(alertDto);

        return alertDto;
    }

    @Transactional
    public void markAsRead(Long id) {
        alertRepository.markAsRead(id);
    }

    @Transactional
    public void markAllAsRead() {
        alertRepository.markAllAsRead();
    }

    public long getUnreadCount() {
        return alertRepository.countByIsReadFalse();
    }

    private AlertDto toDto(Alert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .ticker(alert.getSymbol().getTicker())
                .symbolName(alert.getSymbol().getName())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .triggerValue(alert.getTriggerValue())
                .thresholdValue(alert.getThresholdValue())
                .isRead(alert.getIsRead())
                .timestamp(alert.getTimestamp())
                .build();
    }
}