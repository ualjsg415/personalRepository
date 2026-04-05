export interface MatchPlayerStats {
  name: string;
  role: string;
  kills: number;
  deaths: number;
  assists: number;
  cs: number;
  visionScore: number;
  kda: string;
}

export interface MatchGameInfo {
  title: string;
  teams: [MatchPlayerStats[], MatchPlayerStats[]];
}

export interface MatchTeamInfo {
  name: string;
  iconCandidates: string[];
  fallbackIcon: string;
}

export interface MatchInfo {
  id: number;
  title: string;
  teams: [MatchTeamInfo, MatchTeamInfo];
  games: MatchGameInfo[];
}