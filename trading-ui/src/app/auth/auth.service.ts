import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

  // 🔥 FIX: removed /api
  private baseUrl = 'http://localhost:8081/api/auth';

  private currentUser: any = null;

  constructor(private http: HttpClient) {}

  signup(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/signup`, payload);
  }

  login(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, payload);
  }

  setUser(user: any) {
    this.currentUser = user;
  }

  getUser() {
    return this.currentUser;
  }

  isLoggedIn(): boolean {
    return this.currentUser != null;
  }

  logout() {
    this.currentUser = null;
  }
}
