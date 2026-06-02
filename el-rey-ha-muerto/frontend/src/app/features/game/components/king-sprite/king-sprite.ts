import { Component, Input } from '@angular/core';

export type KingMood = 'idle' | 'happy' | 'worried' | 'dead' | 'sleeping';

@Component({
  selector: 'app-king-sprite',
  imports: [],
  templateUrl: './king-sprite.html',
  styleUrl: './king-sprite.scss',
})
export class KingSprite {
  @Input() mood: KingMood = 'idle';
}
