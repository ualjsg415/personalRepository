import {ChangeDetectorRef, Component, inject} from '@angular/core';
import {HousingLocation} from '../housing-location/housing-location';
import {MatchInfo} from '../housinglocation';
import {HousingService} from '../housing.service';
@Component({
  selector: 'app-home',
  imports: [HousingLocation],
  template: `
    <section class="search-panel">
      <h2>Partidas LCK obtenidas por scraping</h2>
      <form class="search-form">
        <input type="text" placeholder="Filtrar por equipo" #filter />
        <button class="primary" type="button" (click)="filterResults(filter.value)">Buscar</button>
      </form>
    </section>
    <section class="results">
      @for (match of filteredMatchList; track match.id) {
        <app-housing-location [match]="match" />
      } @empty {
        <p class="empty-state">No hay enfrentamientos para ese filtro.</p>
      }
    </section>
  `,
  styleUrls: ['./home.css'],
})
export class Home {
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  matchList: MatchInfo[] = [];
  housingService: HousingService = inject(HousingService);
  filteredMatchList: MatchInfo[] = [];

  constructor() {
    this.housingService.getAllMatches().then((matchList: MatchInfo[]) => {
      this.matchList = matchList;
      this.filteredMatchList = matchList;
      this.changeDetectorRef.markForCheck();
    });
  }

  filterResults(text: string) {
    const query = text.trim().toLowerCase();
    if (!query) {
      this.filteredMatchList = this.matchList;
      return;
    }

    this.filteredMatchList = this.matchList.filter((match) =>
      match.teams.some((team) => team.name.toLowerCase().includes(query)),
    );
  }
}
