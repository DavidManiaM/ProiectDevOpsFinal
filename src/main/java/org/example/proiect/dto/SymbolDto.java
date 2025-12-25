package org.example.proiect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.proiect.model.Symbol;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolDto {
    private Long id;
    private String ticker;
    private String name;
    private Symbol.AssetType type;
    private LocalDateTime createdAt;
}

