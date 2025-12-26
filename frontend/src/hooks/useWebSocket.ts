import { useEffect, useState, useCallback } from 'react';
import wsService from '../services/websocket';
import { Price, Alert } from '../services/api';

export function useWebSocket() {
  const [connected, setConnected] = useState(false);
  const [prices, setPrices] = useState<Map<string, Price>>(new Map());
  const [alerts, setAlerts] = useState<Alert[]>([]);

  useEffect(() => {
    wsService.connect();

    const checkConnection = setInterval(() => {
      setConnected(wsService.isConnected());
    }, 1000);

    const unsubPrice = wsService.onPriceUpdate((price: Price) => {
      setPrices(prev => {
        const newMap = new Map(prev);
        newMap.set(price.ticker, price);
        return newMap;
      });
    });

    const unsubAlert = wsService.onAlert((alert: Alert) => {
      setAlerts(prev => [alert, ...prev].slice(0, 50));
    });

    return () => {
      clearInterval(checkConnection);
      unsubPrice();
      unsubAlert();
      wsService.disconnect();
    };
  }, []);

  const subscribeToSymbol = useCallback((ticker: string, handler: (price: Price) => void) => {
    return wsService.subscribeToSymbol(ticker, handler);
  }, []);

  return {
    connected,
    prices: Array.from(prices.values()),
    alerts,
    subscribeToSymbol,
  };
}

