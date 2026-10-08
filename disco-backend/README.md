# Disco Elysium Backend & Roll Engine

A lightweight, zero-dependency Go HTTP server and 2d6 skill-check engine powering the Disco Check widget, browser emulator, and local preview environments.

---

## Architecture Overview

```
disco-backend/
├── main.go               # HTTP routing, CORS middleware, directory resolution
├── engine/
│   ├── dice.go           # 2d6 roll engine, critical resolution, accent mapping
│   ├── quotes.go         # 24-skill dataset, authentic dialogue, asset mappings
│   └── engine_test.go    # 14 unit tests for randomness, bounds, and data integrity
└── assets/
    ├── dice/             # Pixelated die faces (die1.png – die6.png)
    └── skills/           # Skill portrait artwork (2214×3072 JPEG)
        ├── Intellect/    # Logic, Encyclopedia, Rhetoric, Drama, Conceptualization, Visual Calculus
        ├── Psyche/       # Volition, Inland Empire, Empathy, Authority, Suggestion, Esprit de Corps
        ├── Physique/     # Endurance, Pain Threshold, Physical Instrument, Electrochemistry, Shivers, Half Light
        └── Motorics/     # Hand/Eye Coordination, Perception, Reaction Speed, Savoir Faire, Interfacing, Composure
```

---

## HTTP Endpoints

| Endpoint | Method | Response | Description |
|----------|--------|----------|-------------|
| `/api/roll` | `GET` | `application/json` | Rolls 2d6, resolves criticals, and returns random skill data |
| `/api/health` | `GET` | `application/json` | Health check (`{"status": "ok"}`) |
| `/assets/*` | `GET` | Binary (PNG/JPG) | Static file server for dice icons and skill portrait artwork |
| `/dashboard` | `GET` | `text/html` | Serves `android/dashboard.html` for browser-based testing |
| `/` | `GET` | Static assets | Serves `web/` (`index.html`, `emulator.html`, `widget.js`) |

All API endpoints include permissive CORS headers (`Access-Control-Allow-Origin: *`) allowing client connections from local development servers, emulators, or web preview hosts.

---

## 2d6 Roll Engine Mechanics

### 1. Dice & Probabilities
The engine rolls two 6-sided dice ($2d6$) using `math/rand` seeded by Unix nanoseconds:
* **Snake Eyes ($1 + 1$):** Triggers a **Critical Failure**. The roll total is 2, the quote is fixed to the iconic catastrophic failure text, the portrait defaults to Half Light, and the accent color becomes `#D71921` (Crimson).
* **Boxcars ($6 + 6$):** Triggers a **Critical Success**. The roll total is 12, the quote is fixed to the transcendent double-sixes line, the portrait defaults to Volition, and the accent color becomes `#7D6BB3` (Radiant Violet).
* **Any other roll ($3 – 11$):** Uniformly selects one of the 24 game skills, returns its authentic voice quote, relative portrait asset path, and the attribute group's accent color.

### 2. Attribute Groups & Accent Colors

| Attribute | Skills | Accent Color | Hex |
|-----------|--------|--------------|-----|
| **Intellect** | Logic, Encyclopedia, Rhetoric, Drama, Conceptualization, Visual Calculus | Warm Gold | `#C4A35A` |
| **Psyche** | Volition, Inland Empire, Empathy, Authority, Suggestion, Esprit de Corps | Deep Purple | `#8170B2` |
| **Physique** | Endurance, Pain Threshold, Physical Instrument, Electrochemistry, Shivers, Half Light | Muted Rose | `#A84F63` |
| **Motorics** | Hand/Eye Coordination, Perception, Reaction Speed, Savoir Faire, Interfacing, Composure | Seafoam Teal | `#6C9B9A` |

---

## JSON Payload Schema (`/api/roll`)

```json
{
  "die1": 3,
  "die2": 5,
  "total": 8,
  "header": "LOGIC",
  "quote": "Do it for the picture puzzle. Put it all together. Solve the world. One conversation at a time.",
  "asset_path": "Intellect/Logic.jpg",
  "is_critical": false,
  "die1_asset": "dice/die3.png",
  "die2_asset": "dice/die5.png",
  "accent_color": "#C4A35A"
}
```

### Critical Failure Example:
```json
{
  "die1": 1,
  "die2": 1,
  "total": 2,
  "header": "CRITICAL FAILURE",
  "quote": "Two ones stare back at you like empty eye sockets. The universe simply refuses to cooperate.",
  "asset_path": "Physique/Half_Light.jpg",
  "is_critical": true,
  "die1_asset": "dice/die1.png",
  "die2_asset": "dice/die1.png",
  "accent_color": "#D71921"
}
```

---

## Running Locally

### Start the Server
```sh
go run ./disco-backend
```
Server listens at `http://localhost:8080`.

### Smoke Test with cURL
```sh
# Check health
curl http://localhost:8080/api/health

# Run a roll
curl http://localhost:8080/api/roll
```

---

## Testing & Quality Assurance

The backend includes a dedicated unit test suite in `engine/engine_test.go`:

```sh
go test -v ./disco-backend/engine/...
```

### What the tests cover:
1. **`TestNewEngine`**: Engine instantiates with all 24 skills initialized.
2. **`TestRollReturnsDice`**: Evaluates 200 consecutive rolls to guarantee dice values stay strictly in the range $[1, 6]$.
3. **`TestRollDieAssets`**: Validates generated die asset strings format correctly as `dice/die[1-6].png`.
4. **`TestCriticalFailureViaTestable`**: Ensures forced $1+1$ rolls produce the correct header, quote, Half Light portrait, critical flag, and red accent.
5. **`TestCriticalSuccessViaTestable`**: Ensures forced $6+6$ rolls produce the correct header, quote, Volition portrait, critical flag, and purple accent.
6. **`TestRegularRollPopulatesFields`**: Confirms all regular rolls generate complete non-empty responses.
7. **`TestAccentColors`**: Validates accurate mapping of stat group prefixes to their exact design system hex colors.
8. **`TestGetAllSkillsCount`**: Asserts exactly 24 skills are registered.
9. **`TestGetAllSkillsNoEmptyFields`**: Ensures every skill has a populated name, quote, and valid asset file path.
10. **`TestAssetPathPrefix`**: Validates asset paths begin with a recognized stat category (`Intellect/`, `Psyche/`, `Physique/`, `Motorics/`).
11. **`TestInitDiscoElysiumQuotesHasAllKeys`**: Verifies all required keys (including `snake_eyes` and `boxcars`) exist in the quote dictionary.
12. **`TestTestableEngineCritFail` / `CritSuccess` / `Regular`**: Validates deterministic injected RNG behaves properly across critical and normal paths.
