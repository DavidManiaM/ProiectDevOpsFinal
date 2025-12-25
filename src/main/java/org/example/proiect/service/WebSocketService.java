package org.example.proiect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.AlertDto;
import org.example.proiect.dto.PriceDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String PRICES_TOPIC = "/topic/prices";
    private static final String ALERTS_TOPIC = "/topic/alerts";
    private static final String PRICE_TOPIC_PREFIX = "/topic/price/";

    public void broadcastPriceUpdate(PriceDto price) {
        // Broadcast to general prices topic
        messagingTemplate.convertAndSend(PRICES_TOPIC, price);

        // Broadcast to specific symbol topic
        messagingTemplate.convertAndSend(PRICE_TOPIC_PREFIX + price.getTicker(), price);

        log.debug("Broadcasted price update for {}: {}", price.getTicker(), price.getPrice());
    }

    public void broadcastAlert(AlertDto alert) {
        messagingTemplate.convertAndSend(ALERTS_TOPIC, alert);
        log.info("Broadcasted alert for {}: {}", alert.getTicker(), alert.getMessage());
    }

    public void sendToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
}

