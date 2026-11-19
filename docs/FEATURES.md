# Feature comparison and limits

The uploaded ZIP is source for Kakashi’s Rust Launcher Mod v1.4 and its companion settings app. It is not a complete standalone launcher source tree. Its patched launcher requires the manufacturer runtime. The older v7.5 mod targets a different launcher engine. Aura uses an independent, portable HOME implementation instead.

| Source capability | Aura implementation |
|---|---|
| Dock background, radius, margin, height and icon offset | Portable wallpaper-backed dock with separate controls; width fits pins and can scroll on narrow screens |
| Home/dock glass, 15 parameters each | Independent material groups; GPU blur on Android 12+; translucent fallback on older devices; refraction/reflection are visual approximations, not Xiaomi’s shader |
| Glass file/share/clipboard presets | Implemented; legacy presets converted approximately and bounded |
| Search capsule, mic and provider choice | App/web search capsule, voice via installed recognizer, Google/DuckDuckGo/ChatGPT/Perplexity browser handoff |
| -1 screen, App Vault / Discover | Aura Today page; Xiaomi App Vault and Google Discover integration are not ported |
| Hide apps, custom names, icon packs and icon style | Implemented within Aura; does not hide apps at the Android OS level |
| Larger icons and highlights | Global icon size and clipped shapes/highlights; proprietary per-app super-icon artwork is not included |
| Free widget resize and widget stacks | Android AppWidgetHost, host-frame resizing and stack cycling; providers retain their own constraints and licensing |
| Widgets on App Vault / paid widgets | Not ported; provider/licensing requirements remain in force |
| Stacked system recents, OriginOS transition, global iOS-style app animation | Not ported: controlled by Android’s system launcher/Quickstep and OEM runtime |
| Recent apps gesture | Own five-slot momentum carousel; bottom swipe and two-second hold; app resume, own-history removal; optional consented local previews on Android 11+ |
| Drawer blur | Aura drawer uses Home glass |
| Home backup | Portable layout, folders, dock, labels and settings; widget providers must be rebound on a new device |
| Tablet / desktop | Dedicated adaptive layouts, menu bar, keyboard/mouse controls, functional file-provider browser and external-display presentation |
| Desktop app windows | Uses Android app launches; optional bounds request on freeform-capable devices; no universal window-manager replacement |

## Added Home controls

Classic and App Drawer modes, split swipe-down Control Center/Notifications, Home edge gestures, optional cross-app navigation helper, high-density icon loading, phone trackpad and user-selected desktop input method are implemented independently. The helper observes package names only and never retrieves window text. Notification content is displayed only after Android notification access is enabled. Screenshots are optional private cached previews; no network permission is declared.

Native Android recents task deletion and OEM app transitions remain system features. Physical desktop display/IME focus and global navigation need device validation and appropriate Android grants.

## Verification

Publication is gated on Android 8 and Android 15 emulator jobs. The build runs 119 responsive geometry checks, 44 gesture/workspace checks and 19 Android integration checks for launch, app discovery, the three layouts, persistent dock/folders/names, backup handling, drawer, settings and glass screens. The emulator cannot establish that every third-party widget, icon pack, file provider or physical display works on every device. Those require device testing. No manufacturer-only feature is advertised as unlocked on generic Android.

## Sources inspected

- Uploaded HyperOS-Rust-Launcher-Mod-1.4.zip; AGPL-3.0, existing source and notices inspected locally.
- https://github.com/Mods-Center/HyperOS-Rust-Launcher-Mod/releases/tag/v1.4
- https://github.com/Mods-Center/HyperOS-Launcher (v7.5 feature list and changelog).

The emulator jobs use extra CPU/RAM and allow Android to settle before interaction checks. They do not disable the stock launcher. The rendered-layout check waits for Aura’s ready workspace rather than taking a screenshot after a fixed delay.
