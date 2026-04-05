import {Component, input} from '@angular/core';
import {MatchInfo} from '../housinglocation';
import {RouterLink} from '@angular/router';
@Component({
  selector: 'app-housing-location',
  imports: [RouterLink],
  template: `
    <section class="match-card">
      <p class="match-id">Partida #{{ match().id }}</p>
      <h3 class="match-title">{{ match().title }}</h3>
      <div class="teams-row">
        <figure class="team">
          <img
            class="team-icon"
            [src]="getTeamIcon(0)"
            alt=""
            (error)="onTeamIconError(0)"
          />
          <figcaption>{{ match().teams[0].name }}</figcaption>
        </figure>
        <span class="versus">VS</span>
        <figure class="team">
          <img
            class="team-icon"
            [src]="getTeamIcon(1)"
            alt=""
            (error)="onTeamIconError(1)"
          />
          <figcaption>{{ match().teams[1].name }}</figcaption>
        </figure>
      </div>
      <p class="series-meta">{{ match().games.length }} games disponibles</p>
      <a [routerLink]="['/details', match().id]">Mas informacion</a>
    </section>
  `,
  styleUrls: ['./housing-location.css'],
})
export class HousingLocation {
  match = input.required<MatchInfo>();
  private iconIndexes = [0, 0];
  private useFallbackIcon = [false, false];

  getTeamIcon(teamIndex: number): string {
    const team = this.match().teams[teamIndex];

    if (this.useFallbackIcon[teamIndex]) {
      return team.fallbackIcon;
    }

    return team.iconCandidates[this.iconIndexes[teamIndex]] ?? team.fallbackIcon;
  }

  onTeamIconError(teamIndex: number): void {
    const team = this.match().teams[teamIndex];
    const nextIndex = this.iconIndexes[teamIndex] + 1;

    if (nextIndex < team.iconCandidates.length) {
      this.iconIndexes[teamIndex] = nextIndex;
      return;
    }

    this.useFallbackIcon[teamIndex] = true;
  }
}