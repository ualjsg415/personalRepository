import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth';
import { GameService } from '../../../core/services/game';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  email        = '';
  password     = '';
  showPassword = false;
  loading      = false;
  error        = '';

  constructor(
    private router: Router,
    private authService: AuthService,
    private gameService: GameService,
    private cdr: ChangeDetectorRef
  ) {}

  togglePassword() { this.showPassword = !this.showPassword; }

  login() {
    if (!this.email.trim() || !this.password.trim()) return;
    this.loading = true;
    this.error   = '';

    this.authService.login(this.email, this.password).pipe(
      switchMap(user => this.gameService.startGame(user.username))
    ).subscribe({
      next: (gameState) => this.router.navigate(['/game'], { state: { gameState } }),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message
          || (err.status === 401 ? 'Credenciales incorrectas, Majestad' : 'No se pudo conectar con el reino');
        this.cdr.detectChanges();
      }
    });
  }

  goRegister()       { this.router.navigate(['/register']); }
  goForgotPassword() { this.router.navigate(['/forget-password']); }
  goHome()           { this.router.navigate(['/home']); }
}
