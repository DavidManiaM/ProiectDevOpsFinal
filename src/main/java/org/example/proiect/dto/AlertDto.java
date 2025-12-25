package org.example.proiect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.proiect.model.Alert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDto {
    private Long id;
    private String ticker;
    private String symbolName;
    private Alert.AlertType alertType;
    private String message;
    private BigDecimal triggerValue;
    private BigDecimal thresholdValue;
    private Boolean isRead;
    private LocalDateTime timestamp;
}

