import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-narrative-box',
  imports: [],
  templateUrl: './narrative-box.html',
  styleUrl: './narrative-box.scss',
})
export class NarrativeBox {
  @Input() title = '';
  @Input() text = '';
}
