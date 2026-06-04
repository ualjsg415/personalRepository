import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class RegisterPage {
  name            = '';
  email           = '';
  password        = '';
  confirmPassword = '';
  showPassword    = false;

  constructor(private router: Router) {}

  togglePassword() { this.showPassword = !this.showPassword; }

  register() {
    if (!this.name.trim() || !this.email.trim() || !this.password.trim()) return;
    this.router.navigate(['/login']);
  }

  goLogin() { this.router.navigate(['/login']); }
  goHome()  { this.router.navigate(['/home']); }
}
