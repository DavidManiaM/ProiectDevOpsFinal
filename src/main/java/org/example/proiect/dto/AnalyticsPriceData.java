package org.example.proiect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AnalyticsPriceData {
    private String ticker;
    private BigDecimal price;
    private BigDecimal volume;

    @JsonProperty("moving_average_5")
    private BigDecimal movingAverage5;

    @JsonProperty("moving_average_20")
    private BigDecimal movingAverage20;

    @JsonProperty("percent_change")
    private BigDecimal percentChange;

    private LocalDateTime timestamp;

    @JsonProperty("is_anomaly")
    private boolean anomaly;

    @JsonProperty("anomaly_type")
    private String anomalyType;

    @JsonProperty("anomaly_message")
    private String anomalyMessage;
}

