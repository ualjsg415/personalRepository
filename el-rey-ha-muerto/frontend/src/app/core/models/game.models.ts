export type SceneId = 'bedroom' | 'throne-room' | 'dining-hall' | 'gardens' | 'dungeon';

export interface KingStats {
  hygiene: number;
  hunger: number;
  popularity: number;
  wealth: number;
}

export interface Choice {
  id: number;
  label: 'A' | 'B' | 'C';
  text: string;
}

export interface GameEvent {
  id: number;
  title: string;
  description: string;
  scene: SceneId;
  choices: Choice[];
}

export interface GameState {
  sessionId: number;
  playerName: string;
  day: number;
  isAlive: boolean;
  stats: KingStats;
  currentEvent: GameEvent | null;
  narratorMessage: string;
  causeOfDeath?: string;
}

export interface GameSession {
  id: number;
  playerName: string;
  startedAt: string;
  endedAt?: string;
  daysSurvived: number;
  isAlive: boolean;
  causeOfDeath?: string;
}
