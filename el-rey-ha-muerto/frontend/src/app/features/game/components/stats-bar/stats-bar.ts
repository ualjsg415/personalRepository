import { Component, Input, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { KingStats } from '../../../../core/models/game.models';

@Component({
  selector: 'app-stats-bar',
  imports: [],
  templateUrl: './stats-bar.html',
  styleUrl: './stats-bar.scss',
})
export class StatsBar implements OnChanges {
  @Input() stats: KingStats = { hygiene: 100, hunger: 100, popularity: 50, wealth: 100 };
  @Input() day = 1;
  @Input() playerName = 'Rey';

  deltas: { [key: string]: number } = {};
  private prevStats: KingStats | null = null;
  private deltaTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['stats']) {
      const curr = changes['stats'].currentValue as KingStats;

      if (this.prevStats && !changes['stats'].firstChange) {
        const newDeltas: { [key: string]: number } = {};
        for (const key of ['hygiene', 'hunger', 'popularity', 'wealth'] as const) {
          const diff = curr[key] - this.prevStats[key];
          if (diff !== 0) newDeltas[key] = diff;
        }
        this.deltas = newDeltas;

        if (this.deltaTimer) clearTimeout(this.deltaTimer);
        this.deltaTimer = setTimeout(() => {
          this.deltas = {};
          this.cdr.detectChanges();
        }, 1800);
      }

      this.prevStats = { ...curr };
    }
  }

  get statList() {
    return [
      { key: 'hygiene',    icon: '🧼', label: 'Higiene',     value: this.stats.hygiene },
      { key: 'hunger',     icon: '🍗', label: 'Hambre',      value: this.stats.hunger },
      { key: 'popularity', icon: '👑', label: 'Popularidad', value: this.stats.popularity },
      { key: 'wealth',     icon: '💰', label: 'Riqueza',     value: this.stats.wealth },
    ];
  }

  get deathStat(): string | null {
    if (this.stats.hygiene    === 0) return 'hygiene';
    if (this.stats.hunger     === 0) return 'hunger';
    if (this.stats.popularity === 0) return 'popularity';
    if (this.stats.wealth     === 0) return 'wealth';
    return null;
  }

  barColor(value: number): string {
    if (value > 60) return '#27ae60';
    if (value > 30) return '#f0c94a';
    return '#c0392b';
  }
}
