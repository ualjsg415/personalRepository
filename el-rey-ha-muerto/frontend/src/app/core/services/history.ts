import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameSession, SessionDetail } from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class HistoryService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/history';

  getSessions(): Observable<GameSession[]> {
    return this.http.get<GameSession[]>(this.base);
  }

  getSessionDetail(id: number): Observable<SessionDetail> {
    return this.http.get<SessionDetail>(`${this.base}/${id}`);
  }
}
