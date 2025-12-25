package org.example.proiect.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    private Counter priceUpdatesCounter;
    private Counter alertsCounter;
    private Timer processingTimer;

    @PostConstruct
    public void init() {
        priceUpdatesCounter = Counter.builder("stock_market.price_updates")
                .description("Number of price updates processed")
                .tag("application", "stock-market-gateway")
                .register(meterRegistry);

        alertsCounter = Counter.builder("stock_market.alerts")
                .description("Number of alerts generated")
                .tag("application", "stock-market-gateway")
                .register(meterRegistry);

        processingTimer = Timer.builder("stock_market.processing_time")
                .description("Time taken to process price data")
                .tag("application", "stock-market-gateway")
                .register(meterRegistry);
    }

    public void incrementPriceUpdates() {
        priceUpdatesCounter.increment();
    }

    public void incrementAlerts() {
        alertsCounter.increment();
    }

    public void recordProcessingTime(long milliseconds) {
        processingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(processingTimer);
    }
}