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

  constructor(private router: Router) {}

  register() {
    if (!this.name.trim() || !this.email.trim() || !this.password.trim()) return;
    this.router.navigate(['/login']);
  }

  goLogin() { this.router.navigate(['/login']); }
  goHome()  { this.router.navigate(['/home']); }
}
