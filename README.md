# Disco Check — v2.0

A 2d6 skill-check widget for Android with authentic *Disco Elysium* quotes. Comes
as a home-screen app widget and a full-screen preview activity, both powered by the
same 24-skill dataset as the Go backend.

---

## What's new in v2.0

| Area | Change |
|------|--------|
| **Roll engine** | Full 24-skill dataset (Intellect · Psyche · Physique · Motorics) with authentic quotes |
| **Criticals** | Snake-eyes (1+1) → **CRITICAL FAILURE**, boxcars (6+6) → **CRITICAL SUCCESS** |
| **Volition Cooldown** | 10-second roll cooldown — rapid re-taps summon **VOLITION** to urge patience |
| **Colours** | Per-stat accent: Intellect gold · Psyche purple · Physique rose · Motorics teal |
| **Tests** | 14 Go engine unit tests (dice range, asset filenames, critical paths, skill completeness) |
| **Emulator** | Self-contained browser emulator at `/emulator.html` — no backend required |
| **CI** | Three-job workflow: engine tests → signed APK → GitHub Release with install notes |

---

## Backend Engine & API

The Go backend (`disco-backend/`) provides the canonical 2d6 engine and asset server. Full details are in [`disco-backend/README.md`](disco-backend/README.md).

### Key Features
* **Standard REST API**: Simple, zero-dependency Go HTTP endpoints serving JSON rolls and binary assets.
* **CORS-enabled**: Permissive headers for easy pairing with any frontend or local test harness.
* **Deterministic Engine Interface**: Designed with injected RNG interfaces for repeatable unit testing.

### API Endpoints
* `GET /api/roll` — Rolls 2d6, calculates criticals, and returns skill text, asset paths, and hex accent color.
* `GET /api/health` — Returns `{"status":"ok"}`.
* `GET /assets/*` — Serves dice icons and high-resolution skill portraits.
* `GET /emulator.html` — Serves the interactive browser emulator.

---

## Local development

### Browser playground (no Android Studio needed)

```sh
go run ./disco-backend
```

| URL | Purpose |
|-----|---------|
| `http://localhost:8080` | Widget preview (`web/index.html`) |
| `http://localhost:8080/emulator.html` | Interactive emulator — force any scenario, see roll log & stats |
| `http://localhost:8080/dashboard` | Android dashboard preview |
| `http://localhost:8080/api/roll` | Raw JSON roll endpoint |
| `http://localhost:8080/api/health` | Health check |

### Run engine tests

```sh
go test -v ./disco-backend/engine/...
```

### Run full test + bundle build

```sh
./test-widget.sh
```

### Build the sideloadable widget bundle

```sh
./build-widget.sh
# Output: dist/widget.js  dist/widget.json  dist/assets/
```

---

## Android APK

### Build & release via GitHub Actions (recommended)

1. Push this repository to GitHub.
2. Tag and push `v2.0.0`:

   ```sh
   git tag v2.0.0
   git push origin v2.0.0
   ```

3. GitHub Actions runs three jobs:
   - **Go engine tests** — all 14 unit tests must pass
   - **Build signed APK** — Gradle assembles `disco-check-v2.0.0.apk`
   - **Publish GitHub Release** — APK attached as a downloadable asset with install notes

4. On your phone: download the APK → open → install.

For a build without a tag (e.g. testing CI), use **Actions → Disco Check v2.0 — Build & Release APK → Run workflow**.

### Persistent signing (keep updates installable without uninstalling)

Without secrets, each CI run generates a fresh self-signed key and Android will
require the old app to be uninstalled before updating. To avoid this, add these
four repository secrets under **Settings → Secrets → Actions**:

| Secret | Value |
|--------|-------|
| `DISCO_KEYSTORE_BASE64` | `base64 < your-release.keystore` |
| `DISCO_KEYSTORE_PASSWORD` | Store password |
| `DISCO_KEY_ALIAS` | Key alias |
| `DISCO_KEY_PASSWORD` | Key password |

Generate a keystore once:

```sh
keytool -genkeypair -v \
  -keystore disco-release.keystore \
  -storepass YOUR_STORE_PASS \
  -keypass   YOUR_KEY_PASS \
  -alias     disco-check \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Disco Check, O=YourName, C=US"

base64 < disco-release.keystore | pbcopy   # macOS — paste into DISCO_KEYSTORE_BASE64
```

### ADB install (USB)

```sh
adb install -r disco-check-v2.0.0.apk
```

---

## Physical Nothing Phone / widget launcher testing

1. Enable **Developer Mode for Widgets** in Nothing OS Launcher settings.
2. Connect over USB and verify: `adb devices`
3. Install the APK via ADB or sideload.
4. Long-press the home screen → **Widgets** → **Disco Check** → drag to place.
5. Tap the widget to roll.

For sideloading the raw widget bundle into a community hub:

```sh
./build-widget.sh
adb push dist/ <community-widget-import-directory>
```

The exact importer path is hub-version specific; consult the hub documentation.

---

## Architecture

```
DiscoElysiumProject/
├── src/widget.js              # Distributable widget (Nothing widget contract)
├── web/
│   ├── index.html             # Browser widget preview
│   ├── widget.js              # Widget JS (used by index.html + Android WebView)
│   └── emulator.html          # Self-contained emulator (no backend needed)
├── android/
│   └── app/src/main/java/…/
│       ├── MainActivity.java  # WebView activity + JS roll bridge (24 skills)
│       └── DiscoWidgetProvider.java  # Home-screen widget (24 skills, native)
├── disco-backend/
│   ├── main.go                # HTTP server (Go)
│   └── engine/
│       ├── dice.go            # 2d6 engine + RollResponse
│       ├── quotes.go          # 24-skill dataset
│       └── engine_test.go     # 14 unit tests
├── .github/workflows/
│   └── android-apk.yml        # Test → Build → Release workflow
├── build-widget.sh            # Bundle builder
└── test-widget.sh             # Full test + bundle verify
```
