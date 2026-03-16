import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = `${environment.apiBaseUrl}/auth`;
  private readonly TOKEN_KEY = 'jwt_token';

  constructor(private http: HttpClient) {}

  signup(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, payload);
  }

  login(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, payload);
  }

  forgotPassword(payload: { email: string }) {
    return this.http.post(
      `${this.baseUrl}/forgot-password`,
      payload
    );
  }

  resetPassword(payload: { token: string, newPassword: string }) {
    return this.http.post(
      `${this.baseUrl}/reset-password`,
      payload
    );
  }

  // 🔑 JWT handling
  setToken(token: string) {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    return token;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
  }
}
