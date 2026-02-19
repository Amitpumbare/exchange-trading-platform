import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InstrumentService } from '../../core/instrument.service';

@Injectable({ providedIn: 'root' })
export class OrdersService {

  private baseUrl = 'http://localhost:8081/orders';

  constructor(
    private http: HttpClient,
    private instrumentService: InstrumentService
  ) {}

  getOrders(): Observable<any> {
    return this.http.get(`${this.baseUrl}/get-orders`);
  }

  createOrder(payload: any): Observable<any> {

    const inst = this.instrumentService.getInstrument();

    if(!inst){
      throw new Error("instrument not selected");
    }

    const finalPayload = {
      instrumentId: inst.id,
      type: payload.type,
      price: payload.price,
      quantity: payload.quantity
    };

    return this.http.post(`${this.baseUrl}/place-orders`, finalPayload);
  }

  cancelOrder(orderId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/cancel-order/${orderId}`, {});
  }

  modifyOrder(orderId: number, payload: any): Observable<any> {

    const inst = this.instrumentService.getInstrument();

    if(!inst){
      throw new Error("instrument not selected");
    }

    const finalPayload = {
      instrumentId: inst.id,
      type: payload.type,
      price: payload.price,
      quantity: payload.quantity
    };

    return this.http.put(`${this.baseUrl}/modify-order/${orderId}`, finalPayload);
  }
}
