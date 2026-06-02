import { Component, Input } from '@angular/core';
import { SceneId } from '../../../../core/models/game.models';

@Component({
  selector: 'app-scene-background',
  imports: [],
  templateUrl: './scene-background.html',
  styleUrl: './scene-background.scss',
})
export class SceneBackground {
  @Input() scene: SceneId = 'bedroom';
}
