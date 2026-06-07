import { Component, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-how-to-play',
  standalone: true,
  imports: [],
  templateUrl: './how-to-play.html',
  styleUrl: './how-to-play.scss',
})
export class HowToPlay {
  @Output() closed = new EventEmitter<void>();

  close() { this.closed.emit(); }
}
