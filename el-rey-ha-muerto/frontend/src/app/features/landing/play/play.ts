import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-play',
  imports: [],
  templateUrl: './play.html',
  styleUrl: './play.scss',
})
export class PlayPage {
  constructor(private router: Router) {}
  jugar() { this.router.navigate(['/home']); }
}
