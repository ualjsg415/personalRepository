import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forget-password',
  imports: [FormsModule],
  templateUrl: './forget-password.html',
  styleUrl: './forget-password.scss',
})
export class ForgetPasswordPage {
  email = '';
  sent  = false;

  constructor(private router: Router) {}

  send() {
    if (!this.email.trim()) return;
    this.sent = true;
  }

  goBack() { this.router.navigate(['/home']); }
}
