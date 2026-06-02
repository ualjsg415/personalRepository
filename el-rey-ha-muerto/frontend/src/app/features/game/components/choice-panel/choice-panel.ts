import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Choice } from '../../../../core/models/game.models';

@Component({
  selector: 'app-choice-panel',
  imports: [],
  templateUrl: './choice-panel.html',
  styleUrl: './choice-panel.scss',
})
export class ChoicePanel {
  @Input() choices: Choice[] = [];
  @Input() disabled = false;
  @Output() choiceMade = new EventEmitter<Choice>();

  select(choice: Choice) {
    if (!this.disabled) this.choiceMade.emit(choice);
  }
}
