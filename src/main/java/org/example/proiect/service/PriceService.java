package org.example.proiect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.AnalyticsPriceData;
import org.example.proiect.dto.PriceDto;
import org.example.proiect.model.Alert;
import org.example.proiect.model.Price;
import org.example.proiect.model.Symbol;
import org.example.proiect.repository.PriceRepository;
import org.example.proiect.repository.SymbolRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

    private final PriceRepository priceRepository;
    private final SymbolRepository symbolRepository;
    private final AlertService alertService;
    private final WebSocketService webSocketService;

    public List<PriceDto> getLatestPrices() {
        return priceRepository.findLatestPricesForAllSymbols().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PriceDto> getPriceHistory(String ticker, int limit) {
        return priceRepository.findByTickerOrderByTimestampDesc(ticker.toUpperCase(), PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PriceDto getLatestPrice(String ticker) {
        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));

        Price price = priceRepository.findTopBySymbolOrderByTimestampDesc(symbol)
                .orElseThrow(() -> new RuntimeException("No price data for: " + ticker));

        return toDto(price);
    }

    public List<PriceDto> getRecentVariations(String ticker, int hours) {
        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return priceRepository.findRecentPrices(symbol, since).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PriceDto savePriceFromAnalytics(AnalyticsPriceData data) {
        Symbol symbol = symbolRepository.findByTicker(data.getTicker().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + data.getTicker()));

        Price price = Price.builder()
                .symbol(symbol)
                .price(data.getPrice())
                .volume(data.getVolume())
                .movingAverage5(data.getMovingAverage5())
                .movingAverage20(data.getMovingAverage20())
                .percentChange(data.getPercentChange())
                .timestamp(data.getTimestamp() != null ? data.getTimestamp() : LocalDateTime.now())
                .build();

        price = priceRepository.save(price);
        log.debug("Saved price for {}: {}", data.getTicker(), data.getPrice());

        PriceDto priceDto = toDto(price);

        // Broadcast price update via WebSocket
        webSocketService.broadcastPriceUpdate(priceDto);

        // Create alert if anomaly detected
        if (data.isAnomaly()) {
            Alert.AlertType alertType = determineAlertType(data.getAnomalyType());
            alertService.createAlert(symbol, alertType, data.getAnomalyMessage(),
                    data.getPrice(), data.getPercentChange());
        }

        return priceDto;
    }

    private Alert.AlertType determineAlertType(String anomalyType) {
        if (anomalyType == null) return Alert.AlertType.ANOMALY;

        return switch (anomalyType.toUpperCase()) {
            case "SPIKE_UP" -> Alert.AlertType.SPIKE_UP;
            case "SPIKE_DOWN" -> Alert.AlertType.SPIKE_DOWN;
            case "VOLUME_SURGE" -> Alert.AlertType.VOLUME_SURGE;
            default -> Alert.AlertType.ANOMALY;
        };
    }

    private PriceDto toDto(Price price) {
        return PriceDto.builder()
                .id(price.getId())
                .ticker(price.getSymbol().getTicker())
                .symbolName(price.getSymbol().getName())
                .price(price.getPrice())
                .volume(price.getVolume())
                .movingAverage5(price.getMovingAverage5())
                .movingAverage20(price.getMovingAverage20())
                .percentChange(price.getPercentChange())
                .timestamp(price.getTimestamp())
                .build();
    }
}