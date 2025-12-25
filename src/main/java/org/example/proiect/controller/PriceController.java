package org.example.proiect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.proiect.dto.PriceDto;
import org.example.proiect.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Tag(name = "Prices", description = "Stock price endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PriceController {

    private final PriceService priceService;

    @GetMapping
    @Operation(summary = "Get latest prices", description = "Returns the latest price for each symbol")
    public ResponseEntity<List<PriceDto>> getLatestPrices() {
        return ResponseEntity.ok(priceService.getLatestPrices());
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Get latest price for symbol", description = "Returns the most recent price for a specific symbol")
    public ResponseEntity<PriceDto> getLatestPrice(@PathVariable String ticker) {
        return ResponseEntity.ok(priceService.getLatestPrice(ticker));
    }

    @GetMapping("/{ticker}/history")
    @Operation(summary = "Get price history", description = "Returns historical prices for a symbol")
    public ResponseEntity<List<PriceDto>> getPriceHistory(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(priceService.getPriceHistory(ticker, limit));
    }

    @GetMapping("/{ticker}/variations")
    @Operation(summary = "Get recent variations", description = "Returns price variations for a symbol in the last N hours")
    public ResponseEntity<List<PriceDto>> getRecentVariations(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(priceService.getRecentVariations(ticker, hours));
    }
}