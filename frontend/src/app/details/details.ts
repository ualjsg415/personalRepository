import {ChangeDetectorRef, Component, inject} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {HousingService} from '../housing.service';
import {MatchInfo} from '../housinglocation';
@Component({
  selector: 'app-details',
  template: `
    @if (match) {
      <article class="details-layout">
        <header class="match-header">
          <h2>{{ match.title }}</h2>
          <p>{{ match.games.length }} games con ALL STATS completos</p>
        </header>

        <section class="games-grid">
          @for (game of match.games; track $index; let gameIndex = $index) {
            <article
              class="game-card"
              [style.--game-accent]="gameCardAccents[gameIndex % gameCardAccents.length]"
            >
              <header class="game-card-header">
                <h3>{{ game.title }}</h3>
                <span>ALL STATS</span>
              </header>

              <div class="teams-stats">
                <section class="team-stats">
                  <h4>{{ match.teams[0].name }}</h4>
                  <table>
                    <thead>
                      <tr>
                        <th>Jugador</th>
                        <th>Rol</th>
                        <th>K</th>
                        <th>D</th>
                        <th>A</th>
                        <th>CS</th>
                        <th>Vision</th>
                        <th>KDA</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (player of game.teams[0]; track $index) {
                        <tr>
                          <td>{{ player.name }}</td>
                          <td>{{ player.role }}</td>
                          <td>{{ player.kills }}</td>
                          <td>{{ player.deaths }}</td>
                          <td>{{ player.assists }}</td>
                          <td>{{ player.cs }}</td>
                          <td>{{ player.visionScore }}</td>
                          <td>{{ player.kda }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </section>

                <section class="team-stats">
                  <h4>{{ match.teams[1].name }}</h4>
                  <table>
                    <thead>
                      <tr>
                        <th>Jugador</th>
                        <th>Rol</th>
                        <th>K</th>
                        <th>D</th>
                        <th>A</th>
                        <th>CS</th>
                        <th>Vision</th>
                        <th>KDA</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (player of game.teams[1]; track $index) {
                        <tr>
                          <td>{{ player.name }}</td>
                          <td>{{ player.role }}</td>
                          <td>{{ player.kills }}</td>
                          <td>{{ player.deaths }}</td>
                          <td>{{ player.assists }}</td>
                          <td>{{ player.cs }}</td>
                          <td>{{ player.visionScore }}</td>
                          <td>{{ player.kda }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </section>
              </div>
            </article>
          }
        </section>
      </article>
    } @else {
      <section class="empty-state">
        <h2>No se encontro el enfrentamiento</h2>
        <p>Vuelve al inicio para seleccionar otra serie.</p>
      </section>
    }
  `,
  styleUrls: ['./details.css'],
})
export class Details {
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  route: ActivatedRoute = inject(ActivatedRoute);
  housingService = inject(HousingService);
  match: MatchInfo | undefined;
  gameCardAccents = ['#0f766e', '#1d4ed8', '#ea580c', '#be185d', '#14532d'];

  constructor() {
    const matchId = parseInt(this.route.snapshot.params['id'], 10);
    this.housingService.getMatchById(matchId).then((match) => {
      this.match = match;
      this.changeDetectorRef.markForCheck();
    });
  }
}