package org.example.proiect.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.proiect.dto.PriceDto;
import org.example.proiect.service.PriceService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final PriceService priceService;

    @MessageMapping("/subscribe")
    @SendTo("/topic/prices")
    public List<PriceDto> subscribeToAllPrices() {
        log.info("Client subscribed to all prices");
        return priceService.getLatestPrices();
    }

    @MessageMapping("/subscribe/{ticker}")
    @SendTo("/topic/price/{ticker}")
    public PriceDto subscribeToSymbol(@DestinationVariable String ticker) {
        log.info("Client subscribed to symbol: {}", ticker);
        return priceService.getLatestPrice(ticker);
    }
}