import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/auth';
  private STORAGE_KEY = 'elrey_user';

  register(username: string, email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.base}/register`, { username, email, password }).pipe(
      tap(user => this.saveUser(user))
    );
  }

  login(email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.base}/login`, { email, password }).pipe(
      tap(user => this.saveUser(user))
    );
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
  }

  getCurrentUser(): AuthUser | null {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  getToken(): string | null {
    return this.getCurrentUser()?.token ?? null;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private saveUser(user: AuthUser): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
  }
}
