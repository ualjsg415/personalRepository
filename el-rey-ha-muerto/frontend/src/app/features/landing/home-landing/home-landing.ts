import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home-landing',
  imports: [],
  templateUrl: './home-landing.html',
  styleUrl: './home-landing.scss',
})
export class HomeLanding {
  constructor(private router: Router) {}
  play()         { this.router.navigate(['/login']); }
  goToRegister() { this.router.navigate(['/register']); }
  history()      { this.router.navigate(['/history']); }
}
