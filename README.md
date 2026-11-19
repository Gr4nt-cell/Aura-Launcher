# Aura Launcher

A standalone Android Home app with adaptive phone, tablet and desktop layouts. Package: `com.gr4nt.auralauncher`. Requires Android 8.0/API 26 or newer. No root, Xiaomi runtime, accessibility service, or network permission is required.

## Install

Install `Aura-Launcher.apk`, open it and choose **Default Home app** from settings or the Home menu. Select Aura Launcher in Android’s Home app picker. The supplied launcher mod is not required and this APK does not replace the system launcher package.

## Included

- Adaptive phone, tablet and desktop Home layouts, with manual layout selection.
- Installed-app discovery, searchable drawer, custom app labels, hidden icons, icon packs, rounded/circle/square shapes, monochrome icons when provided by an app, icon highlights and adjustable icon size.
- Three persistent Home pages, folders, drag-to-folder, dock pinning, a dock that fits its contents with scrolling when necessary, and a Today page.
- Separate Home and dock glass controls: 15 material parameters, geometry, tone, icon offset and search-capsule spacing. GPU wallpaper blur on Android 12+, translucent fallback on Android 8–11.
- Glass presets through files, clipboard and sharing; imports the supplied extension’s preset format with a bounded approximate parameter conversion.
- Android widget hosting, resizing and stacks with explicit cycling; provider configuration and binding use Android’s permission flow.
- Desktop menu bar, live clock/battery, app dock, app search, recent-launch shortcuts, keyboard shortcuts, right-click menus, and a desktop presentation on a connected display.
- Files: Android document picker and scoped folder browser with open, rename, copy/paste, share, create folder and provider-supported deletion. Folder access is granted by the user.
- Wallpaper picker and layout/settings backup and restore. Widget IDs and wallpaper URI grants are device-specific and are not exported.

## Scope

This implements portable versions of compatible features from the supplied Kakashi mod design. It does **not** contain Xiaomi’s native launcher, does not transplant its proprietary animation engine, and does not claim that every feature from the older mod has been ported. See [the feature comparison](docs/FEATURES.md).

This is a desktop-style Android launcher, not macOS. It launches Android apps. Android controls system recents, global app-opening animations, window management, permissions and hardware support. Freeform launch bounds are requested only when the device advertises freeform-window support; individual apps or the system may ignore them. External-display app launching is subject to Android’s display-access rules. There are no pretend app windows or OS controls.

## Build and signing

GitHub Actions builds with JDK 21, Android SDK 35 and standard Android build tools. Configure `AURA_KEYSTORE_BASE64`, `AURA_KEYSTORE_PASSWORD`, `AURA_KEY_ALIAS`, and `AURA_KEY_PASSWORD` for stable production signing. Without these secrets, each run uses a temporary development key: a later differently signed build requires uninstalling the previous build. Do not use development-signed previews for a production rollout. Private signing material is never committed or published.

Run `bash tools/build.sh` with `ANDROID_HOME` set to an SDK containing API 35 and build-tools 35.0.0. The corresponding source is published alongside the APK.

## License and credits

AGPL-3.0. Aura adaptation by Gr4nt. Feature-design references: Kakashi / Mods Center’s supplied Rust Launcher Mod v1.4 and older Launcher Mod v7.5; HyperChanger / ColdP for the glass-control design. Original attribution and license notices are retained in NOTICE.md and LICENSE. The new portable Java renderer is independent of Xiaomi’s Rust/Flutter binary and does not require native patches.
