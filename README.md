# Aura Launcher

A standalone Android Home app with adaptive phone, tablet and desktop layouts. Package: `com.gr4nt.auralauncher`. Requires Android 8.0/API 26 or newer. No root, Xiaomi runtime or Internet permission is required. Home works independently; cross-app navigation is an optional, explicitly enabled accessibility feature.

## Install

Install `Aura-Launcher.apk`, open it and choose **Default Home app** from settings or the Home menu. Select Aura Launcher in Android’s Home app picker. The supplied launcher mod is not required and this APK does not replace the system launcher package.

## New mobile and input controls

- HyperOS-inspired mobile Home: large clock, icon grid, page indicators, compact search and pinned-app glass dock. Native app icons load at high density; original icon packs remain supported.
- Choose **Classic** (all visible apps across desktop pages) or **App Drawer** (swipe up for all apps). Classic swipe-up opens Google search, with a browser fallback if Google is absent.
- On Home, swipe down on the right for Aura Control Center and on the left for Aura Notifications. Notification access is optional. Real notification actions and dismissal are supported.
- Swipe inward from either edge for Back; swipe from the bottom for Home; swipe up from the bottom and keep the finger still for two seconds for Aura Recents.
- Own momentum-scrolling recent-app carousel uses the iOS Dialer favorites profile: center scale 1, neighbors .84 and .66, with opacity 1/.85/.55. Cards resume apps; removing a card clears Aura history, not the Android task.
- Phone keyboard/trackpad controls for a connected desktop display. Select **Aura Desktop Keyboard** for typing into the focused external editor. Optional helper enables cross-app display clicks/swipes on Android 11+; cursor overlay needs Android’s overlay grant. Aura’s own desktop surface also accepts local pointer input.

The optional helper’s bottom gesture strip sits just above Android’s navigation area, so it does not compete for mandatory system-bar touches. Android’s navigation remains available as a fallback.

For cross-app gestures, open Aura settings → Cross-app gesture helper, read the disclosure, then enable the service in Android. It observes foreground package names, not window text. Optional Android 11+ previews are stored only in private cache, expire after 12 hours and are erased when the helper stops. Secure windows show a placeholder. Backup/import cannot grant helper consent or export previews/history. The keyboard commits only keys explicitly pressed and does not read or store editor text.

Control Center changes media volume, requests torch access, controls authorized media sessions and opens real Wi-Fi/Bluetooth/data settings. Global brightness/rotation require Android’s modify-settings grant; without it brightness is explicitly Aura-only.

## Included

- Adaptive phone, tablet and desktop Home layouts, with manual layout selection.
- Installed-app discovery, searchable drawer, custom app labels, hidden icons, icon packs, rounded/circle/square shapes, monochrome icons when provided by an app, icon highlights and adjustable icon size.
- Three saved drawer-mode Home pages, automatically paged Classic apps, folders, drag-to-folder, dock pinning, a dock that fits its contents with scrolling when necessary, and a Today page.
- Separate Home and dock glass controls: 15 material parameters, geometry, tone, icon offset and search-capsule spacing. GPU wallpaper blur on Android 12+, translucent fallback on Android 8–11.
- Glass presets through files, clipboard and sharing; imports the supplied extension’s preset format with a bounded approximate parameter conversion.
- Android widget hosting, resizing and stacks with explicit cycling; provider configuration and binding use Android’s permission flow.
- Desktop menu bar, live clock/battery, app dock, app search, recent-launch shortcuts, keyboard shortcuts, right-click menus, and a desktop presentation on a connected display.
- Files: Android document picker and scoped folder browser with open, rename, copy/paste, share, create folder and provider-supported deletion. Folder access is granted by the user.
- Wallpaper picker and layout/settings backup and restore. Widget IDs and wallpaper URI grants are device-specific and are not exported.

## Scope

This implements portable versions of compatible features from the supplied Kakashi mod design. It does **not** contain Xiaomi’s native launcher, does not transplant its proprietary animation engine, and does not claim that every feature from the older mod has been ported. See [the feature comparison](docs/FEATURES.md).

This is a desktop-style Android launcher, not macOS. It launches Android apps. Aura Recents is an independent app switcher; Android controls native task snapshots, task removal, global app-opening animations, window management, permissions and hardware support. Freeform launch bounds are requested only when the device advertises freeform-window support; individual apps or the system may ignore them. External-display app launching is subject to Android’s display-access rules. There are no pretend app windows or OS controls.

Physical external-display focus, selected input method and OEM gesture conflicts require testing on your phone/display combination. A normal installed launcher cannot guarantee an identical OEM system gesture pipeline.

## Build and signing

GitHub Actions builds with JDK 21, Android SDK 35 and standard Android build tools. Configure `AURA_KEYSTORE_BASE64`, `AURA_KEYSTORE_PASSWORD`, `AURA_KEY_ALIAS`, and `AURA_KEY_PASSWORD` for stable production signing. Without these secrets, each run uses a temporary development key: a later differently signed build requires uninstalling the previous build. Do not use development-signed previews for a production rollout. Private signing material is never committed or published.

Run `bash tools/build.sh` with `ANDROID_HOME` set to an SDK containing API 35 and build-tools 35.0.0. The corresponding source is published alongside the APK.

## License and credits

AGPL-3.0. Aura adaptation by Gr4nt. Feature-design references: Kakashi / Mods Center’s supplied Rust Launcher Mod v1.4 and older Launcher Mod v7.5; HyperChanger / ColdP for the glass-control design. Original attribution and license notices are retained in NOTICE.md and LICENSE. The new portable Java renderer is independent of Xiaomi’s Rust/Flutter binary and does not require native patches.
