#!/usr/bin/env bash
set -euo pipefail
adb install -r out/Aura-Launcher.apk
adb install -r build/Aura-tests.apk
adb shell cmd package set-home-activity com.gr4nt.auralauncher/.LauncherActivity
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
timeout 150s adb shell am instrument -w com.gr4nt.auralauncher.test/com.gr4nt.auralauncher.AuraSmoke | tee out/android-tests.txt
grep -q 'PASS: 13 Android integration checks' out/android-tests.txt
if grep -q 'FAIL:' out/android-tests.txt; then exit 1; fi
adb shell am force-stop com.gr4nt.auralauncher
adb shell am start -n com.gr4nt.auralauncher/.LauncherActivity
sleep 2
adb exec-out screencap -p > out/Aura-phone.png
adb shell wm size 1200x1920
adb shell wm density 240
sleep 3
adb shell am start -n com.gr4nt.auralauncher/.LauncherActivity
sleep 2
adb exec-out screencap -p > out/Aura-tablet.png
adb shell wm size 1600x1000
adb shell wm density 160
sleep 3
adb shell am start -n com.gr4nt.auralauncher/.LauncherActivity
sleep 2
adb exec-out screencap -p > out/Aura-desktop.png
adb shell wm size reset
adb shell wm density reset
adb logcat -d -s AndroidRuntime:E > out/android-crashes.txt
if grep -q 'Process: com.gr4nt.auralauncher,' out/android-crashes.txt; then cat out/android-crashes.txt; exit 1; fi
if [ ! -s out/android-crashes.txt ]; then printf '%s\n' 'PASS: no AndroidRuntime crash entries during smoke checks.' > out/android-crashes.txt; fi
