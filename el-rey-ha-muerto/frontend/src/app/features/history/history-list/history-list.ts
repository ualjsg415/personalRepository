import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  sessions:  GameSession[] = [];
  loading    = true;
  page       = 1;
  pageSize   = 10;

  constructor(
    private router: Router,
    private historyService: HistoryService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.historyService.getSessions().subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.loading  = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sessions.length / this.pageSize));
  }

  get pagedSessions(): GameSession[] {
    const start = (this.page - 1) * this.pageSize;
    return this.sessions.slice(start, start + this.pageSize);
  }

  get counterStart(): number {
    return (this.page - 1) * this.pageSize;
  }

  prevPage() {
    if (this.page > 1) { this.page--; this.cdr.detectChanges(); }
  }

  nextPage() {
    if (this.page < this.totalPages) { this.page++; this.cdr.detectChanges(); }
  }

  openDetail(id: number) {
    this.router.navigate(['/history', id]);
  }

  goBack() {
    this.router.navigate(['/home']);
  }
}
