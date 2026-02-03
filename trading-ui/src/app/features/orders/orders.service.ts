import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class OrdersService {

  private baseUrl = 'http://localhost:8081/orders';

  constructor(private http: HttpClient) {}

  getOrders(): Observable<any> {
    return this.http.get(`${this.baseUrl}/get-orders`);
  }

  createOrder(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/place-orders`, payload);
  }

  cancelOrder(orderId: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/cancel-order/${orderId}`, {});
  }

  modifyOrder(orderId: number, payload: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/modify-order/${orderId}`, payload);
  }
}
