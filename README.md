# Disco Check widget

The Go backend owns the 2d6 roll and quote selection. `src/widget.js` adapts
that response to the Nothing widget layout contract.

## Test in a browser (no Android Studio)

From the project root:

```sh
go run ./disco-backend
```

Open <http://localhost:8080>. The local playground uses the same `/api/roll`
endpoint and asset URLs as the widget. `GET /api/health` checks reachability.

Build the sideloadable bundle:

```sh
./build-widget.sh
```

The output is `dist/`, containing `widget.js`, `widget.json`, and image assets.
The backend works when started from either the project root or `disco-backend/`.

## Physical Nothing Phone testing

1. Enable Developer Mode for Widgets in Nothing OS / Nothing Launcher settings.
2. Connect the phone over USB and verify it is visible:

   ```sh
   adb devices
   ```

3. Build the bundle, then use the Community Widget Hub local importer to select
   `dist/`. If your Hub exposes an ADB importer, push the same directory using
   its documented destination:

   ```sh
   adb push dist/ <community-widget-import-directory>
   ```

The exact importer directory is OS/Hub-version specific; do not guess it.

## Android APK

This repository now also contains a generic Android APK wrapper in
[`android/`](./android). It packages the widget UI and image assets locally,
so the installed APK can roll without a Go server or `localhost` connection.
It is an installable companion app; Nothing-specific launcher/widget-provider
APIs are separate from this generic APK shell.

### Build on GitHub

1. Create a GitHub repository and push this project:

   ```sh
   git init
   git add .
   git commit -m "Add Disco Check widget and Android APK"
   git branch -M main
   git remote add origin https://github.com/<you>/<repo>.git
   git push -u origin main
   ```

2. Open **Actions → Build Android APK → Run workflow**.
3. Download the `disco-check-debug-apk` artifact.
4. On the phone, enable installation from the browser/files app, open the APK,
   and install it. For USB installation:

   ```sh
   adb install -r app-debug.apk
   ```

Pushing a tag such as `v1.0.0` also runs the workflow:

```sh
git tag v1.0.0
git push origin v1.0.0
```

The first APK is unsigned debug output. A public GitHub release should use a
private signing key stored in GitHub Actions secrets before distributing a
release APK.
