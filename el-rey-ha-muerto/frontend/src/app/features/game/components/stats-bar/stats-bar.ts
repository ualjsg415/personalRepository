import { Component, Input } from '@angular/core';
import { KingStats } from '../../../../core/models/game.models';

@Component({
  selector: 'app-stats-bar',
  imports: [],
  templateUrl: './stats-bar.html',
  styleUrl: './stats-bar.scss',
})
export class StatsBar {
  @Input() stats: KingStats = { hygiene: 100, hunger: 100, popularity: 50, wealth: 100 };
  @Input() day = 1;
  @Input() playerName = 'Rey';

  get statList() {
    return [
      { key: 'hygiene',    icon: '🧼', label: 'Higiene',     value: this.stats.hygiene,    color: '#3a9fd4' },
      { key: 'hunger',     icon: '🍗', label: 'Hambre',      value: this.stats.hunger,     color: '#c0392b' },
      { key: 'popularity', icon: '👑', label: 'Popularidad', value: this.stats.popularity, color: '#f0c94a' },
      { key: 'wealth',     icon: '💰', label: 'Riqueza',     value: this.stats.wealth,     color: '#27ae60' },
    ];
  }

  barColor(value: number): string {
    if (value > 60) return '#27ae60';
    if (value > 30) return '#f0c94a';
    return '#c0392b';
  }
}
