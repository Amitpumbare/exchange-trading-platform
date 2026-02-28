import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InstrumentService } from '../../core/instrument.service';

export interface OrderResponse {

  id: number;

  instrumentSymbol: string;

  type: 'BUY' | 'SELL';

  price: number;

  quantity: number;

  status: 'OPEN' | 'PARTIALLY_FILLED' | 'FILLED' | 'CANCELLED';

  message: string;

}

@Injectable({ providedIn: 'root' })
export class OrdersService {

  private baseUrl = 'http://localhost:8081/orders';

  constructor(
    private http: HttpClient,
    private instrumentService: InstrumentService
  ) {}

  // ✅ FIXED
  getOrders(): Observable<OrderResponse[]> {

    return this.http.get<OrderResponse[]>(
      `${this.baseUrl}/get-orders`
    );

  }

  createOrder(payload: any): Observable<any> {

    const inst = this.instrumentService.getInstrument();

    if (!inst) {
      throw new Error("instrument not selected");
    }

    const finalPayload = {

      instrumentId: inst.instrumentId,

      type: payload.type,

      price: payload.price,

      quantity: payload.quantity

    };

    return this.http.post(
      `${this.baseUrl}/place-orders`,
      finalPayload
    );

  }

  cancelOrder(orderId: number): Observable<any> {

    return this.http.put(
      `${this.baseUrl}/cancel-order/${orderId}`,
      {}
    );

  }

  modifyOrder(orderId: number, payload: any): Observable<any> {

    const inst = this.instrumentService.getInstrument();

    if (!inst) {
      throw new Error("instrument not selected");
    }

    const finalPayload = {

      instrumentId: inst.instrumentId,

      type: payload.type,

      price: payload.price,

      quantity: payload.quantity

    };

    return this.http.put(
      `${this.baseUrl}/modify-order/${orderId}`,
      finalPayload
    );

  }

}
