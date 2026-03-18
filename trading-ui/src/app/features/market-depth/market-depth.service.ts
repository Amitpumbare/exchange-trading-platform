import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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

  private baseUrl = `${environment.apiBaseUrl}/instruments`;

  constructor(private http: HttpClient) {}

  getDepth(instrumentId: string, depth: number = 10): Observable<OrderBookResponse> {

    return this.http.get<OrderBookResponse>(
      `${this.baseUrl}/${instrumentId}/depth?depth=${depth}`
    );

  }

}
