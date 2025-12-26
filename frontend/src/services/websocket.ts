import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

export type PriceUpdateHandler = (price: any) => void;
export type AlertHandler = (alert: any) => void;

class WebSocketService {
  private client: Client | null = null;
  private priceHandlers: PriceUpdateHandler[] = [];
  private alertHandlers: AlertHandler[] = [];
  private connected = false;

  connect() {
    if (this.client?.connected) {
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected');
      this.connected = true;

      // Subscribe to prices topic
      this.client?.subscribe('/topic/prices', (message: IMessage) => {
        const price = JSON.parse(message.body);
        this.priceHandlers.forEach(handler => handler(price));
      });

      // Subscribe to alerts topic
      this.client?.subscribe('/topic/alerts', (message: IMessage) => {
        const alert = JSON.parse(message.body);
        this.alertHandlers.forEach(handler => handler(alert));
      });
    };

    this.client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      this.connected = false;
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client?.connected) {
      this.client.deactivate();
    }
    this.connected = false;
  }

  onPriceUpdate(handler: PriceUpdateHandler) {
    this.priceHandlers.push(handler);
    return () => {
      this.priceHandlers = this.priceHandlers.filter(h => h !== handler);
    };
  }

  onAlert(handler: AlertHandler) {
    this.alertHandlers.push(handler);
    return () => {
      this.alertHandlers = this.alertHandlers.filter(h => h !== handler);
    };
  }

  subscribeToSymbol(ticker: string, handler: PriceUpdateHandler) {
    if (!this.client?.connected) {
      console.warn('WebSocket not connected');
      return () => {};
    }

    const subscription = this.client.subscribe(`/topic/price/${ticker}`, (message: IMessage) => {
      const price = JSON.parse(message.body);
      handler(price);
    });

    return () => subscription.unsubscribe();
  }

  isConnected() {
    return this.connected;
  }
}

export const wsService = new WebSocketService();
export default wsService;

