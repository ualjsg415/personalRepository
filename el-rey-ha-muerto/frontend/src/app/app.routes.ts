import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'play', pathMatch: 'full' },
  {
    path: 'play',
    loadComponent: () => import('./features/landing/play/play')
      .then(m => m.PlayPage)
  },
  {
    path: 'home',
    loadComponent: () => import('./features/landing/home-landing/home-landing')
      .then(m => m.HomeLanding)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/landing/login/login')
      .then(m => m.LoginPage)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/landing/register/register')
      .then(m => m.RegisterPage)
  },
  {
    path: 'forget-password',
    loadComponent: () => import('./features/landing/forget-password/forget-password')
      .then(m => m.ForgetPasswordPage)
  },
  {
    path: 'game',
    loadComponent: () => import('./features/game/game-page/game-page')
      .then(m => m.GamePage),
    canActivate: [authGuard]
  },
  {
    path: 'history',
    loadComponent: () => import('./features/history/history-list/history-list')
      .then(m => m.HistoryList),
    canActivate: [authGuard]
  },
  {
    path: 'history/:id',
    loadComponent: () => import('./features/history/history-detail/history-detail')
      .then(m => m.HistoryDetail),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: 'play' }
];
