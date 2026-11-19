#!/usr/bin/env bash
set -euo pipefail
adb install -r out/Aura-Launcher.apk
adb install -r build/Aura-tests.apk
adb shell cmd package set-home-activity com.gr4nt.auralauncher/.LauncherActivity
# Emulator fixture only: prevent the unused stock Quickstep from blocking captures with an ANR.
# No such command exists in the installed Aura app.
if [ "$(adb shell getprop ro.build.version.sdk | tr -d '\r')" -ge 35 ]; then
  adb shell pm disable-user --user 0 com.google.android.apps.nexuslauncher >/dev/null 2>&1 || true
fi
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
timeout 150s adb shell am instrument -w com.gr4nt.auralauncher.test/com.gr4nt.auralauncher.AuraSmoke | tee out/android-tests.txt
grep -q 'PASS: 14 Android integration checks' out/android-tests.txt
if grep -q 'FAIL:' out/android-tests.txt; then exit 1; fi
for screen in Aura-drawer.png Aura-glass.png Aura-settings.png; do adb pull "/sdcard/Android/data/com.gr4nt.auralauncher/files/$screen" "out/$screen"; done
wait_home() {
  echo "Checking rendered $2 layout"
  for attempt in $(seq 1 12); do
    timeout 15s adb shell uiautomator dump /sdcard/aura-layout.xml >/dev/null 2>&1 || true
    adb pull /sdcard/aura-layout.xml "out/$2-layout.xml" >/dev/null 2>&1 || true
    if grep -q "text=\"$1\"" "out/$2-layout.xml" && grep -q 'content-desc="Calendar"' "out/$2-layout.xml"; then return 0; fi
    sleep 1
  done
  cat "out/$2-layout.xml" 2>/dev/null || true
  echo "FAIL: $2 Home did not render the expected responsive layout"
  exit 1
}
adb shell am force-stop com.gr4nt.auralauncher
timeout 30s adb shell am start -W -n com.gr4nt.auralauncher/.LauncherActivity
wait_home Aura phone
adb exec-out screencap -p > out/Aura-phone.png
adb shell wm size 1200x1920
adb shell wm density 240
sleep 3
timeout 30s adb shell am start -W -n com.gr4nt.auralauncher/.LauncherActivity
wait_home Aura tablet
adb exec-out screencap -p > out/Aura-tablet.png
adb shell wm size 1600x1000
adb shell wm density 160
sleep 3
timeout 30s adb shell am start -W -n com.gr4nt.auralauncher/.LauncherActivity
wait_home AURA desktop
adb exec-out screencap -p > out/Aura-desktop.png
adb shell wm size reset
adb shell wm density reset
adb logcat -d -s AndroidRuntime:E > out/android-crashes.txt
if grep -q 'Process: com.gr4nt.auralauncher,' out/android-crashes.txt; then cat out/android-crashes.txt; exit 1; fi
if [ ! -s out/android-crashes.txt ]; then printf '%s\n' 'PASS: no AndroidRuntime crash entries during smoke checks.' > out/android-crashes.txt; fi
