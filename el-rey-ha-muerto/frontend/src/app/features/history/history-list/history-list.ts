import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GameSession } from '../../../core/models/game.models';
import { HistoryService } from '../../../core/services/history';

@Component({
  selector: 'app-history-list',
  imports: [],
  templateUrl: './history-list.html',
  styleUrl: './history-list.scss',
})
export class HistoryList implements OnInit {
  sessions: GameSession[] = [];
  loading = true;

  constructor(private router: Router, private historyService: HistoryService) {}

  ngOnInit() {
    this.historyService.getSessions().subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openDetail(id: number) {
    this.router.navigate(['/history', id]);
  }

  goBack() {
    this.router.navigate(['/start']);
  }
}
