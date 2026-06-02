import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StatsBar } from '../components/stats-bar/stats-bar';
import { SceneBackground } from '../components/scene-background/scene-background';
import { KingSprite, KingMood } from '../components/king-sprite/king-sprite';
import { NarrativeBox } from '../components/narrative-box/narrative-box';
import { ChoicePanel } from '../components/choice-panel/choice-panel';
import { GameState, Choice, SceneId } from '../../../core/models/game.models';

@Component({
  selector: 'app-game-page',
  imports: [StatsBar, SceneBackground, KingSprite, NarrativeBox, ChoicePanel],
  templateUrl: './game-page.html',
  styleUrl: './game-page.scss',
})
export class GamePage implements OnInit {
  state: GameState = {
    sessionId: 0,
    playerName: 'Rey',
    day: 1,
    isAlive: true,
    stats: { hygiene: 100, hunger: 80, popularity: 50, wealth: 100 },
    currentEvent: {
      id: 1,
      title: 'La Mañana del Rey',
      description: 'Su Majestad abre un ojo. Luego el otro. El pijama real huele a gloria y abandono. Hay que lavarse los dientes... o no.',
      scene: 'bedroom',
      choices: [
        { id: 1, label: 'A', text: 'Usar el cepillo de dientes real' },
        { id: 2, label: 'B', text: 'Usar la escobilla del váter' },
        { id: 3, label: 'C', text: 'Seguir durmiendo, ¡soy el REY!' },
      ]
    },
    narratorMessage: '',
  };

  kingMood: KingMood = 'idle';
  choosing = false;

  constructor(private router: Router) {}

  ngOnInit() {
    const nav = history.state;
    if (nav?.playerName) this.state.playerName = nav.playerName;
  }

  get scene(): SceneId {
    return this.state.currentEvent?.scene ?? 'bedroom';
  }

  onChoiceMade(choice: Choice) {
    if (this.choosing) return;
    this.choosing = true;
    this.kingMood = 'worried';
    setTimeout(() => {
      this.kingMood = 'idle';
      this.choosing = false;
    }, 800);
  }

  goBack() {
    this.router.navigate(['/start']);
  }
}
