import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameState } from '../models/game.models';

@Injectable({ providedIn: 'root' })
export class GameService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/game';

  startGame(playerName: string): Observable<GameState> {
    return this.http.post<GameState>(`${this.base}/start`, { playerName });
  }

  makeChoice(sessionId: number, choiceId: number): Observable<GameState> {
    return this.http.post<GameState>(`${this.base}/${sessionId}/choose`, { choiceId });
  }

  getState(sessionId: number): Observable<GameState> {
    return this.http.get<GameState>(`${this.base}/${sessionId}/state`);
  }
}
