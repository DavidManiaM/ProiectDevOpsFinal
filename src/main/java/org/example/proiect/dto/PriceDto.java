package org.example.proiect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDto {
    private Long id;
    private String ticker;
    private String symbolName;
    private String symbolType;
    private BigDecimal price;
    private BigDecimal volume;
    private BigDecimal movingAverage5;
    private BigDecimal movingAverage20;
    private BigDecimal percentChange;
    private LocalDateTime timestamp;
}
