import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { StatsBar } from '../components/stats-bar/stats-bar';
import { SceneBackground } from '../components/scene-background/scene-background';
import { KingSprite, KingMood } from '../components/king-sprite/king-sprite';
import { NarrativeBox } from '../components/narrative-box/narrative-box';
import { ChoicePanel } from '../components/choice-panel/choice-panel';
import { GameState, Choice, SceneId } from '../../../core/models/game.models';
import { GameService } from '../../../core/services/game';

@Component({
  selector: 'app-game-page',
  imports: [StatsBar, SceneBackground, KingSprite, NarrativeBox, ChoicePanel],
  templateUrl: './game-page.html',
  styleUrl: './game-page.scss',
})
export class GamePage implements OnInit {
  state: GameState | null = null;
  kingMood: KingMood = 'idle';
  choosing = false;

  constructor(
    private router: Router,
    private gameService: GameService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const nav = history.state;
    if (nav?.gameState) {
      this.state = nav.gameState;
    } else {
      this.router.navigate(['/login']);
    }
  }

  get scene(): SceneId {
    return this.state?.currentEvent?.scene ?? 'bedroom';
  }

  get daysSurvived(): number {
    if (!this.state) return 0;
    return this.state.isAlive ? this.state.day : this.state.day - 1;
  }

  get isEndgame(): boolean {
    if (!this.state) return false;
    return !this.state.isAlive || (this.state.isAlive && !this.state.currentEvent);
  }

  onChoiceMade(choice: Choice) {
    if (this.choosing || !this.state) return;
    this.choosing = true;
    this.kingMood = 'worried';

    this.gameService.makeChoice(this.state.sessionId, choice.id).subscribe({
      next: (newState) => {
        this.state = newState;
        if (!newState.isAlive) {
          this.kingMood = 'dead';
        } else if (!newState.currentEvent) {
          this.kingMood = 'happy';
        } else {
          this.kingMood = 'idle';
        }
        this.choosing = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.kingMood = 'idle';
        this.choosing = false;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    this.router.navigate(['/home']);
  }

  goToHistory() {
    this.router.navigate(['/history']);
  }
}
