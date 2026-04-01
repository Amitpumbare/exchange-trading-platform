import { Injectable, NgZone } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { Subject, ReplaySubject } from 'rxjs'; // ✅ import Subject
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private client: Client | undefined;
  private subscribed = false;
  private connecting = false;

  private depthSubscription?: StompSubscription;


  tradeEvents$ = new Subject<any>();
  orderEvents$ = new Subject<any>();
  depthEvents$ = new ReplaySubject<any>(10);

  constructor(private zone: NgZone) {}

  private getUserIdFromToken(): number | null {

    const token = localStorage.getItem('jwt_token');

    if (!token) return null;

    const payload = JSON.parse(atob(token.split('.')[1]));

    return payload.userId;
  }

  disconnect() {

    if (this.client && this.client.active) {
      this.client.deactivate();
    }

    this.client = undefined;
    this.subscribed = false;
    this.connecting = false;

    this.depthSubscription?.unsubscribe();
  }

  connect() {

    if (this.client?.active || this.connecting) {
      return;
    }

    this.connecting = true;

    this.client = new Client({
      brokerURL: environment.wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onStompError = frame => {
      console.error("STOMP Broker Error:", frame);
    };

    this.client.onWebSocketClose = () => {
      this.connecting = false;
    };

    this.client.onConnect = () => {

      const userId = this.getUserIdFromToken();

      this.connecting = false;

      if (!userId) return;

      if (this.subscribed) return;
      this.subscribed = true;

      // TRADE EVENTS
      this.client?.subscribe(`/topic/trades/${userId}`, message => {

        const trade = JSON.parse(message.body);

        this.zone.run(() => {
          this.tradeEvents$.next(trade);
        });

      });

      // ORDER EVENTS
      this.client?.subscribe(`/topic/orders/${userId}`, message => {

        const order = JSON.parse(message.body);

        this.zone.run(() => {
          this.orderEvents$.next(order);
        });

      });

    };

    this.client.activate();
  }

  subscribeDepth(instrumentId: string) {

    if (!this.client || !this.client.active) return;

    this.depthSubscription?.unsubscribe();

    this.depthSubscription =
      this.client.subscribe(`/topic/depth/${instrumentId}`, message => {

        const depth = JSON.parse(message.body);

        this.zone.run(() => {
          this.depthEvents$.next(depth);
        });

      });

  }

  unsubscribeDepth() {
    this.depthSubscription?.unsubscribe();
  }

}
