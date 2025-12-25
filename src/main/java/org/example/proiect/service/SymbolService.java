package org.example.proiect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.SymbolDto;
import org.example.proiect.model.Symbol;
import org.example.proiect.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SymbolService {

    private final SymbolRepository symbolRepository;

    public List<SymbolDto> getAllSymbols() {
        return symbolRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SymbolDto getSymbolByTicker(String ticker) {
        Symbol symbol = symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));
        return toDto(symbol);
    }

    public Symbol getSymbolEntityByTicker(String ticker) {
        return symbolRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Symbol not found: " + ticker));
    }

    @Transactional
    public SymbolDto createSymbol(SymbolDto dto) {
        if (symbolRepository.existsByTicker(dto.getTicker().toUpperCase())) {
            throw new RuntimeException("Symbol already exists: " + dto.getTicker());
        }

        Symbol symbol = Symbol.builder()
                .ticker(dto.getTicker().toUpperCase())
                .name(dto.getName())
                .type(dto.getType())
                .build();

        symbol = symbolRepository.save(symbol);
        log.info("Created new symbol: {}", symbol.getTicker());
        return toDto(symbol);
    }

    private SymbolDto toDto(Symbol symbol) {
        return SymbolDto.builder()
                .id(symbol.getId())
                .ticker(symbol.getTicker())
                .name(symbol.getName())
                .type(symbol.getType())
                .createdAt(symbol.getCreatedAt())
                .build();
    }
}