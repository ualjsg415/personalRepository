import {Component} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet],
  template: `
    <main class="app-shell">
      <header class="brand-name">
        <a [routerLink]="['/']" class="brand-link">
          <div class="brand-text">
            <h1>LCK Match Cup 2026</h1>
            <p>Tablas de enfrentamientos y ALL STATS por game</p>
          </div>
        </a>
      </header>
      <section class="content">
        <router-outlet />
      </section>
    </main>
  `,
  styleUrls: ['./app.css'],
})
export class App {}