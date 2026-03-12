import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DepthLevel {
  price: number;
  quantity: number;
}

export interface OrderBookResponse {
  bids: DepthLevel[];
  asks: DepthLevel[];
}

@Injectable({
  providedIn: 'root'
})
export class DepthService {

  private baseUrl = 'http://localhost:8081/api/instruments';

  constructor(private http: HttpClient) {}

  getDepth(instrumentId: string, depth: number = 10): Observable<OrderBookResponse> {

    return this.http.get<OrderBookResponse>(
      `${this.baseUrl}/${instrumentId}/depth?depth=${depth}`
    );

  }

}
