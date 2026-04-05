import {Injectable} from '@angular/core';
import {MatchGameInfo, MatchInfo, MatchPlayerStats, MatchTeamInfo} from './housinglocation';

interface ScrapedPlayerStats {
  nombre: string;
  rol: string;
  kills: number;
  deaths: number;
  assists: number;
  cs: number;
  vision_score: number;
}

interface ScrapedGameStats {
  game_titulo: string;
  jugadores: ScrapedPlayerStats[];
}

interface ScrapedMatchStats {
  id?: number;
  enfrentamiento: string;
  games: ScrapedGameStats[];
}

@Injectable({
  providedIn: 'root',
})
export class HousingService {
  private readonly url = 'http://localhost:8080/api/matches';
  private readonly iconBaseUrl = 'https://gol.gg/_img/teams_icon';
  private readonly localTeamIconPath: Record<string, string> = {
    'BNK FearX': '/public/team-icons/bnk.webp',
    'DN SOOPers': '/public/team-icons/dn.webp',
    'Dplus KIA': '/public/team-icons/dplus.webp',
    DRX: '/public/team-icons/drx.webp',
    'Gen.G': '/public/team-icons/geng.webp',
    T1: '/public/team-icons/t1.webp',
  };
  private readonly scrapedTeamIconAlias: Record<string, string> = {
    'BNK FearX': 'BNK_FEARX',
    'DN SOOPers': 'DN_SOOPers',
    'Dplus KIA': 'Dplus_KIA',
    DRX: 'DRX',
    'Gen.G': 'Gen.G',
    T1: 'T1',
  };
  private cachedMatches: MatchInfo[] | null = null;

  async getAllMatches(): Promise<MatchInfo[]> {
    if (this.cachedMatches) {
      return this.cachedMatches;
    }

    try {
      const data = await fetch(this.url);
      if (!data.ok) {
        this.cachedMatches = [];
        return [];
      }

      const rawMatches = (await data.json()) as ScrapedMatchStats[];
      this.cachedMatches = rawMatches.map((match, index) => this.toMatchInfo(match, index));
      return this.cachedMatches;
    } catch {
      this.cachedMatches = [];
      return [];
    }
  }

  async getMatchById(id: number): Promise<MatchInfo | undefined> {
    const matches = await this.getAllMatches();
    return matches.find((match) => match.id === id);
  }

  private toMatchInfo(match: ScrapedMatchStats, index: number): MatchInfo {
    const [teamA, teamB] = this.parseTeams(match.enfrentamiento);

    return {
      id: match.id ?? index + 1,
      title: `${teamA} vs ${teamB}`,
      teams: [this.toTeamInfo(teamA), this.toTeamInfo(teamB)],
      games: match.games.map((game) => this.toGameInfo(game)),
    };
  }

  private toGameInfo(game: ScrapedGameStats): MatchGameInfo {
    const players = game.jugadores.map((player) => this.toPlayerStats(player));
    const splitIndex = Math.ceil(players.length / 2);

    return {
      title: game.game_titulo,
      teams: [players.slice(0, splitIndex), players.slice(splitIndex)],
    };
  }

  private toPlayerStats(player: ScrapedPlayerStats): MatchPlayerStats {
    return {
      name: player.nombre,
      role: player.rol,
      kills: player.kills,
      deaths: player.deaths,
      assists: player.assists,
      cs: player.cs,
      visionScore: player.vision_score,
      kda: this.calculateKda(player.kills, player.deaths, player.assists),
    };
  }

  private parseTeams(matchup: string): [string, string] {
    const teams = matchup
      .split(' vs ')
      .map((team) => team.trim())
      .filter(Boolean);

    if (teams.length >= 2) {
      return [teams[0], teams[1]];
    }

    return [matchup.trim() || 'Team A', 'Team B'];
  }

  private toTeamInfo(teamName: string): MatchTeamInfo {
    return {
      name: teamName,
      iconCandidates: this.buildIconCandidates(teamName),
      fallbackIcon: this.buildFallbackIcon(teamName),
    };
  }

  private buildIconCandidates(teamName: string): string[] {
    const localIcon = this.localTeamIconPath[teamName];
    const explicitAlias = this.scrapedTeamIconAlias[teamName];
    const aliases = explicitAlias ? [explicitAlias, ...this.buildAliases(teamName)] : this.buildAliases(teamName);
    const remoteIcons = aliases.map((alias) => `${this.iconBaseUrl}/${alias}logo_profile.webp`);
    return localIcon ? [localIcon, ...remoteIcons] : remoteIcons;
  }

  private buildAliases(teamName: string): string[] {
    const normalizedName = teamName.trim();
    const manualAliases: Record<string, string[]> = {
      'BNK FearX': ['BNK_FEARX', 'BNK_FearX', 'BFX'],
      'Dplus KIA': ['Dplus_KIA', 'DPLUS_KIA', 'DK'],
      'DN SOOPers': ['DN_SOOPers', 'DN_SOOPERS', 'DN_Soopers', 'DN_Freecs', 'DNF', 'DNFREAKS'],
      DRX: ['DRX'],
      'Gen.G': ['Gen.G', 'GEN.G', 'GenG'],
      T1: ['T1'],
    };

    const generatedAliases = [
      normalizedName,
      normalizedName.replace(/\s+/g, '_'),
      normalizedName.replace(/\s+/g, ''),
      normalizedName.replace(/\s+/g, '_').toUpperCase(),
      normalizedName.replace(/\s+/g, '').toUpperCase(),
      normalizedName.replace(/[^A-Za-z0-9._]/g, ''),
    ];

    const aliases = manualAliases[normalizedName]
      ? [...manualAliases[normalizedName], ...generatedAliases]
      : generatedAliases;

    return Array.from(new Set(aliases.filter(Boolean)));
  }

  private buildFallbackIcon(teamName: string): string {
    const initials =
      teamName
        .split(/\s+/)
        .map((word) => word[0] ?? '')
        .join('')
        .slice(0, 3)
        .toUpperCase() || 'LCK';

    const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='160' height='160'><rect width='100%' height='100%' fill='#0b132b'/><text x='50%' y='54%' text-anchor='middle' dominant-baseline='middle' fill='#f8fbff' font-size='54' font-family='Arial,sans-serif'>${initials}</text></svg>`;

    return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
  }

  private calculateKda(kills: number, deaths: number, assists: number): string {
    const safeDeaths = deaths === 0 ? 1 : deaths;
    return ((kills + assists) / safeDeaths).toFixed(2);
  }
}
