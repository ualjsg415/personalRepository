import puppeteer from "puppeteer";
import fs from "fs/promises";

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function scrapeLCKGames() {
    const browser = await puppeteer.launch({
        headless: false,
        defaultViewport: null
    });

    const page = await browser.newPage();
    const tournamentUrl = "https://gol.gg/tournament/tournament-matchlist/LCK%20Cup%202026/";
    const allMatchesData = [];

    try {
        await page.goto(tournamentUrl, { waitUntil: 'networkidle2' });
        await delay(1000);

        const matchUrls = await page.evaluate(() => {
            const rows = Array.from(document.querySelectorAll('table.table_list tbody tr'));
            return rows.slice(0, 10).map(row => {
                const link = row.querySelector('td a');
                if (!link) return null;
                return new URL(link.getAttribute('href'), document.baseURI).href;
            }).filter(url => url !== null);
        });


        for (let i = 0; i < matchUrls.length; i++) {
            await page.goto(matchUrls[i], { waitUntil: 'networkidle2' });
            await delay(1000);

            const matchName = await page.evaluate(() => {
                const h1 = document.querySelector('h1');
                return h1 ? h1.innerText.trim() : "Enfrentamiento desconocido";
            });

            const gameLinks = await page.evaluate(() => {
                const gameMap = new Map();
                const links = Array.from(document.querySelectorAll('a'));

                links.forEach(a => {
                    const text = a.innerText.trim();
                    const match = text.match(/^Game\s*(\d+)$/i);
                    if (!match) return;

                    const href = a.getAttribute('href');
                    if (!href) return;

                    const gameNumber = Number(match[1]);
                    if (!Number.isFinite(gameNumber)) return;

                    const fullStatsUrl = new URL(href, document.baseURI).href
                        .replace("page-game", "page-fullstats")
                        .replace("page-summary", "page-fullstats");

                    if (!gameMap.has(gameNumber)) {
                        gameMap.set(gameNumber, fullStatsUrl);
                    }
                });

                return Array.from(gameMap.entries())
                    .sort((a, b) => a[0] - b[0])
                    .map(([gameNumber, url]) => ({ gameNumber, url }));
            });

            const matchData = { enfrentamiento: matchName, games: [] };

            for (let j = 0; j < gameLinks.length; j++) {
                await page.goto(gameLinks[j].url, { waitUntil: 'networkidle2' });
                await delay(1000);

                try {
                    await page.waitForSelector('table.completestats', { timeout: 10000 });
                } catch {
                }

                const playersStats = await page.evaluate(() => {
                    const table = document.querySelector('table.completestats');
                    if (!table) return [];

                    const parseNumber = (value) => {
                        const cleaned = String(value ?? '').replace(/[^\d-]/g, '');
                        const parsed = parseInt(cleaned, 10);
                        return Number.isNaN(parsed) ? 0 : parsed;
                    };

                    const rowMap = new Map();
                    const rows = Array.from(table.querySelectorAll('tr'));

                    rows.forEach(row => {
                        const cells = Array.from(row.querySelectorAll('td, th')).map(cell => cell.innerText.trim());
                        if (cells.length < 2) return;

                        const label = cells[0].toLowerCase();
                        if (!label) return;

                        rowMap.set(label, cells.slice(1));
                    });

                    const names = rowMap.get('player') || [];
                    const roles = rowMap.get('role') || [];
                    const kills = rowMap.get('kills') || [];
                    const deaths = rowMap.get('deaths') || [];
                    const assists = rowMap.get('assists') || [];
                    const cs = rowMap.get('cs') || [];
                    const vision = rowMap.get('vision score') || [];

                    const playerCount = Math.max(
                        names.length,
                        roles.length,
                        kills.length,
                        deaths.length,
                        assists.length,
                        cs.length,
                        vision.length
                    );

                    const players = [];
                    for (let index = 0; index < playerCount; index++) {
                        const name = (names[index] || '').trim();
                        if (!name) continue;

                        players.push({
                            nombre: name,
                            rol: (roles[index] || '').trim(),
                            kills: parseNumber(kills[index]),
                            deaths: parseNumber(deaths[index]),
                            assists: parseNumber(assists[index]),
                            cs: parseNumber(cs[index]),
                            vision_score: parseNumber(vision[index])
                        });
                    }

                    return players;
                });

                matchData.games.push({
                    game_titulo: `Game ${gameLinks[j].gameNumber}`,
                    jugadores: playersStats
                });
            }

            allMatchesData.push(matchData);
        }

        await fs.writeFile("lck_fullstats_2026.json", JSON.stringify(allMatchesData, null, 2));

    } catch (e) {
        console.error("ERROR CRÍTICO:", e.message);
    } finally {
        await browser.close();
    }
}

scrapeLCKGames();