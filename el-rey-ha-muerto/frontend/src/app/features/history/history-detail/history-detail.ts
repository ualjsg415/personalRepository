import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionDetail } from '../../../core/models/game.models';
import { HistoryService } from '../../../core/services/history';

@Component({
  selector: 'app-history-detail',
  imports: [],
  templateUrl: './history-detail.html',
  styleUrl: './history-detail.scss',
})
export class HistoryDetail implements OnInit {
  sessionId = 0;
  detail: SessionDetail | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private historyService: HistoryService
  ) {}

  ngOnInit() {
    this.sessionId = Number(this.route.snapshot.paramMap.get('id'));
    this.historyService.getSessionDetail(this.sessionId).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  goBack() {
    this.router.navigate(['/history']);
  }
}
