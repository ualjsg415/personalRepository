import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
    private historyService: HistoryService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.sessionId = Number(this.route.snapshot.paramMap.get('id'));
    this.historyService.getSessionDetail(this.sessionId).subscribe({
      next: (detail) => {
        this.detail = detail;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  toRoman(n: number): string {
    const vals = [10,9,5,4,1];
    const syms = ['X','IX','V','IV','I'];
    let result = '';
    for (let i = 0; i < vals.length; i++) {
      while (n >= vals[i]) { result += syms[i]; n -= vals[i]; }
    }
    return result;
  }

  goBack() {
    this.router.navigate(['/history']);
  }
}
