package org.example.proiect.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.proiect.dto.SymbolDto;
import org.example.proiect.service.SymbolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
@Tag(name = "Symbols", description = "Symbol management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SymbolController {

    private final SymbolService symbolService;

    @GetMapping
    @Operation(summary = "Get all symbols", description = "Returns all available trading symbols")
    public ResponseEntity<List<SymbolDto>> getAllSymbols() {
        return ResponseEntity.ok(symbolService.getAllSymbols());
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Get symbol by ticker", description = "Returns details for a specific symbol")
    public ResponseEntity<SymbolDto> getSymbolByTicker(@PathVariable String ticker) {
        return ResponseEntity.ok(symbolService.getSymbolByTicker(ticker));
    }

    @PostMapping
    @Operation(summary = "Create symbol", description = "Creates a new trading symbol")
    public ResponseEntity<SymbolDto> createSymbol(@RequestBody SymbolDto symbolDto) {
        return ResponseEntity.ok(symbolService.createSymbol(symbolDto));
    }
}

