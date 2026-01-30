import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = 'http://localhost:8081/api/auth';
  private readonly TOKEN_KEY = 'jwt_token';

  constructor(private http: HttpClient) {}

  signup(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, payload);
  }

  login(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, payload);
  }

  // 🔑 JWT handling
  setToken(token: string) {
    console.log('SET TOKEN called, key=', this.TOKEN_KEY, 'value=', token);
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    console.log('GET TOKEN called, key=', this.TOKEN_KEY, 'value=', token);
    return token;
  }


  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
  }
}
