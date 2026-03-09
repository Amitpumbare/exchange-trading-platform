import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private client: Client | undefined;
  private subscribed = false;
  private connecting = false;

  tradeEvents$ = new ReplaySubject<any>(10);
  orderEvents$ = new ReplaySubject<any>(10);

  private getUserIdFromToken(): number | null {

    const token = localStorage.getItem('jwt_token');

    if (!token) return null;

    const payload = JSON.parse(atob(token.split('.')[1]));

    console.log(payload);

    return payload.userId;
  }

  disconnect() {

    if (this.client && this.client.active) {
      this.client.deactivate();
    }

    this.client = undefined;
    this.subscribed = false;
    this.connecting = false;   // ✅ reset connecting state
  }

  connect() {

    console.log("CONNECT CALLED");

    // ✅ Prevent duplicate connect attempts
    if (this.client?.active || this.connecting) {
      return;
    }

    this.connecting = true;

    this.client = new Client({
      brokerURL: 'ws://localhost:8081/ws',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onStompError = frame => {
      console.error("STOMP Broker Error:", frame);
    };

    this.client.onWebSocketClose = () => {
      console.log("WebSocket connection closed");
      this.connecting = false; // ✅ allow reconnect
    };

    this.client.onConnect = () => {

      const userId = this.getUserIdFromToken();

      console.log("WebSocket Connected");

      this.connecting = false; // ✅ connection completed

      if (!userId) {
        console.error("User ID not found in token");
        return;
      }

      if (this.subscribed) return;
      this.subscribed = true;

      this.client?.subscribe(`/topic/trades/${userId}`, message => {

        const trade = JSON.parse(message.body);

        console.log("TRADE EVENT", trade);

        this.tradeEvents$.next(trade);

      });

      this.client?.subscribe(`/topic/orders/${userId}`, message => {

        const order = JSON.parse(message.body);

        console.log("ORDER EVENT", order);

        this.orderEvents$.next(order);

      });

    };

    this.client.activate();
  }

}
