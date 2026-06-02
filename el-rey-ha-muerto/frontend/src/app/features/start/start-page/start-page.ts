import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-start-page',
  imports: [FormsModule],
  templateUrl: './start-page.html',
  styleUrl: './start-page.scss',
})
export class StartPage {
  playerName = '';

  constructor(private router: Router) {}

  startGame() {
    if (!this.playerName.trim()) return;
    this.router.navigate(['/game'], { state: { playerName: this.playerName.trim() } });
  }

  goToHistory() {
    this.router.navigate(['/history']);
  }
}
