package org.example.proiect.controller;

import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.AnalyticsPriceData;
import org.example.proiect.dto.PriceDto;
import org.example.proiect.service.MetricsService;
import org.example.proiect.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal endpoint for receiving data from the analytics service.
 * Not exposed to public API documentation.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class AnalyticsController {

    private final PriceService priceService;
    private final MetricsService metricsService;

    @PostMapping("/price")
    public ResponseEntity<PriceDto> receivePriceData(@RequestBody AnalyticsPriceData data) {
        Timer.Sample sample = metricsService.startTimer();
        try {
            log.debug("Received price data from analytics: {} = {}", data.getTicker(), data.getPrice());
            PriceDto saved = priceService.savePriceFromAnalytics(data);
            metricsService.incrementPriceUpdates();
            return ResponseEntity.ok(saved);
        } finally {
            metricsService.stopTimer(sample);
        }
    }

    @PostMapping("/prices/batch")
    public ResponseEntity<List<PriceDto>> receiveBatchPriceData(@RequestBody List<AnalyticsPriceData> dataList) {
        Timer.Sample sample = metricsService.startTimer();
        try {
            log.debug("Received batch price data from analytics: {} items", dataList.size());
            List<PriceDto> saved = dataList.stream()
                    .map(priceService::savePriceFromAnalytics)
                    .toList();
            metricsService.incrementPriceUpdates();
            return ResponseEntity.ok(saved);
        } finally {
            metricsService.stopTimer(sample);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}