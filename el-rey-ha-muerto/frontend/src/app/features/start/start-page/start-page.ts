import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService } from '../../../core/services/game';

@Component({
  selector: 'app-start-page',
  imports: [FormsModule],
  templateUrl: './start-page.html',
  styleUrl: './start-page.scss',
})
export class StartPage {
  playerName = '';
  loading = false;
  error = '';

  constructor(private router: Router, private gameService: GameService) {}

  startGame() {
    if (!this.playerName.trim() || this.loading) return;
    this.loading = true;
    this.error = '';

    this.gameService.startGame(this.playerName.trim()).subscribe({
      next: (state) => {
        this.router.navigate(['/game'], { state: { gameState: state } });
      },
      error: () => {
        this.loading = false;
        this.error = 'Error al conectar con el reino. ¿Está el servidor activo?';
      }
    });
  }

  goToHistory() {
    this.router.navigate(['/history']);
  }
}
