import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'start', pathMatch: 'full' },
  {
    path: 'start',
    loadComponent: () => import('./features/start/start-page/start-page')
      .then(m => m.StartPage)
  },
  {
    path: 'game',
    loadComponent: () => import('./features/game/game-page/game-page')
      .then(m => m.GamePage)
  },
  {
    path: 'history',
    loadComponent: () => import('./features/history/history-list/history-list')
      .then(m => m.HistoryList)
  },
  {
    path: 'history/:id',
    loadComponent: () => import('./features/history/history-detail/history-detail')
      .then(m => m.HistoryDetail)
  },
  { path: '**', redirectTo: 'start' }
];
