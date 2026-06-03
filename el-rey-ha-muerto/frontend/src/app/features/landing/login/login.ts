import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  email    = '';
  password = '';

  constructor(private router: Router) {}

  login() {
    if (!this.email.trim() || !this.password.trim()) return;
    this.router.navigate(['/game']);
  }

  goRegister()       { this.router.navigate(['/register']); }
  goForgotPassword() { this.router.navigate(['/forget-password']); }
  goHome()           { this.router.navigate(['/home']); }
}
