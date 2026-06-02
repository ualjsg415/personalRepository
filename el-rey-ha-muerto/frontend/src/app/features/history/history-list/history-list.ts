import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { GameSession } from '../../../core/models/game.models';

@Component({
  selector: 'app-history-list',
  imports: [],
  templateUrl: './history-list.html',
  styleUrl: './history-list.scss',
})
export class HistoryList {
  // Datos de prueba — se conectarán al backend en la siguiente fase
  sessions: GameSession[] = [
    { id: 1, playerName: 'Carlos I', startedAt: '2026-06-01T10:00:00', endedAt: '2026-06-01T10:30:00', daysSurvived: 3, isAlive: false, causeOfDeath: 'Infección bucal por escobilla del váter' },
    { id: 2, playerName: 'Felipe II', startedAt: '2026-06-01T11:00:00', endedAt: '2026-06-01T11:45:00', daysSurvived: 7, isAlive: false, causeOfDeath: 'Intoxicación alimentaria por comer piedras' },
    { id: 3, playerName: 'Alfonso X', startedAt: '2026-06-02T09:00:00', daysSurvived: 10, isAlive: true },
  ];

  constructor(private router: Router) {}

  openDetail(id: number) {
    this.router.navigate(['/history', id]);
  }

  goBack() {
    this.router.navigate(['/start']);
  }
}
