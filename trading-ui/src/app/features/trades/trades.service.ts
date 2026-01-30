import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TradesService {

  private baseUrl = 'http://localhost:8081/orders';

  constructor(private http: HttpClient) {}

  getTrades(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/get-trades`);
  }
}
