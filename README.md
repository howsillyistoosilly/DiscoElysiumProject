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
Open <http://localhost:8080/dashboard> to preview and edit all dashboard lines
in a browser. Browser edits are local to that browser; APK edits are stored on
the phone and are shared with its home-screen widget.

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
It includes an editable dashboard and a standard Android home-screen widget
provider. After installing, open **Disco Check** to edit every line. Then
long-press an empty area of the Nothing Launcher home screen, choose
**Widgets**, find **Disco Check**, and drag it onto the home screen. Tap the
widget to roll. If it does not appear immediately, restart Nothing Launcher
or reboot the phone once after installing the APK.

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

2. Create and push a version tag:

   ```sh
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. GitHub Actions builds the APK and creates a **Disco Check v1.0.0** release
   with the APK attached as a downloadable asset. It also uploads the APK as a
   workflow artifact.
4. On the phone, enable installation from the browser/files app, download the
   APK from the GitHub release, and open it to install.

For a build without publishing a release, open **Actions → Build Android APK →
Run workflow** and download the generated artifact.

For USB installation after downloading:

```sh
adb install -r disco-check-v1.0.0.apk
```

GitHub releases are signed with a temporary CI release key so Android can
install them. To update an existing install without uninstalling it, add a
single persistent keystore to GitHub Actions as `DISCO_KEYSTORE_BASE64`, plus
`DISCO_KEYSTORE_PASSWORD`, `DISCO_KEY_ALIAS`, and `DISCO_KEY_PASSWORD`.
Without those secrets, a fresh temporary key is generated for each build and
Android requires the old app to be uninstalled first.
The widget uses standard Android `AppWidgetProvider` APIs and does not require
the Community Widget Hub importer.
