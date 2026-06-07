import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HowToPlay } from '../how-to-play/how-to-play';

@Component({
  selector: 'app-home-landing',
  imports: [HowToPlay],
  templateUrl: './home-landing.html',
  styleUrl: './home-landing.scss',
})
export class HomeLanding {
  showHowToPlay = false;

  constructor(private router: Router) {}

  play()            { this.router.navigate(['/login']); }
  goToRegister()    { this.router.navigate(['/register']); }
  history()         { this.router.navigate(['/history']); }
  toggleHowToPlay() { this.showHowToPlay = !this.showHowToPlay; }
}
