# Goalline

A mobile-first Premier League accumulator tracker inspired by Endzone. Designed for the Samsung Galaxy Z Flip6, including narrow and short viewports. Installable web app; no Android APK is included.

## Features
- Games, Bets and Live pages, horizontal swipes and pull to refresh.
- ESPN fixtures for the selected London calendar date; current date by default.
- Home win / draw / away win only. One outcome per match in each accumulator.
- Compact cards, match details, possession statistics, filters and sorting.
- Persistent expandable betslip, automatic editable names, manual stake and total decimal odds.
- Active bet counts per result, linked bet navigation, live goal gaps, final won/lost states.
- Score-change feed with accumulator status at detection; score corrections supported.
- Browser-local persistence, JSON backup/restore, cached shell and explicit stale/offline state.
- Safe-area padding and keyboard-aware sheet height. No fabricated win probabilities.

## Data
`https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard?dates=YYYYMMDD&limit=100`

This publicly reachable endpoint requires no key and allowed cross-origin requests when checked on 10 October 2026. It is an undocumented ESPN endpoint, not a supported developer API or availability guarantee. Recheck usage terms before wider distribution. No credentials are embedded. Data is fetched directly from the browser every 30 seconds while the page is visible; background updates are not promised. Live scores are never cached by the service worker. A failed request retains visibly stale prior data. A successful empty date shows no fixtures. No mock or frozen fixture data is bundled.

Active bet dates refresh alongside the selected date. Initial Live cards are current match snapshots; subsequent cards record detected changes, including score corrections. Multiple events between polls may appear as a single score update. Impact counts are accumulator statuses at detection, not predicted outcomes. Unrecognised/interrupted statuses stay pending for manual review; bookmaker void and abandonment rules are not automatically applied. Returns are potential gross returns, not guaranteed payouts. Bets only exist in the current browser profile unless backed up.

## Run and verify
Node 22+, no dependencies:

```
npm run check
npm test
npm run build
python3 -m http.server 8000 --directory dist
```

Open http://localhost:8000. PWA installation and service workers require HTTPS (or localhost). The offline shell is available after the first successful visit.

## GitHub Pages
The included workflow tests and publishes `dist` on pushes to main. Repository Settings → Pages must use GitHub Actions. If automatic Pages enablement is refused by GitHub, an administrator must enable it once in repository settings, then rerun the workflow. All asset paths are relative to support `/Goalline/`.

## Verification
Core scoring, draw gaps, result completion, pending states and active counts have automated tests. Real fixture response and cross-origin headers verified during development. Physical Galaxy Flip6 and browser interaction QA still required.
