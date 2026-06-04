import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class RegisterPage {
  name         = '';
  email        = '';
  password     = '';
  showPassword = false;
  loading      = false;
  error        = '';

  constructor(
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  togglePassword() { this.showPassword = !this.showPassword; }

  register() {
    if (!this.name.trim() || !this.email.trim() || !this.password.trim()) return;
    this.loading = true;
    this.error   = '';

    this.authService.register(this.name, this.email, this.password).subscribe({
      next: () => this.router.navigate(['/login']),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Error al fundar el reino';
        this.cdr.detectChanges();
      }
    });
  }

  goLogin() { this.router.navigate(['/login']); }
  goHome()  { this.router.navigate(['/home']); }
}
