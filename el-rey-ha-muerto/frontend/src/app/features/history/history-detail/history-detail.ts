import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-history-detail',
  imports: [],
  templateUrl: './history-detail.html',
  styleUrl: './history-detail.scss',
})
export class HistoryDetail implements OnInit {
  sessionId = 0;

  // Datos de prueba — se conectarán al backend
  mockLog = [
    { day: 1, event: 'La Mañana del Rey', choice: 'Usar la escobilla del váter', hidden: true },
    { day: 2, event: 'El Desayuno Real',  choice: 'Comer las sobras del perro',  hidden: false },
    { day: 3, event: 'La Audiencia',      choice: 'Ignorar al embajador',        hidden: false },
    { day: 4, event: 'El Final',          choice: '—',                           hidden: false, death: 'Infección bucal por escobilla del váter' },
  ];

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.sessionId = Number(this.route.snapshot.paramMap.get('id'));
  }

  goBack() {
    this.router.navigate(['/history']);
  }
}
