#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
ANDROID_JAR="$SDK/platforms/android-35/android.jar"
BT="$SDK/build-tools/35.0.0"
export PATH="$BT:$PATH"
mkdir -p build/classes build/dex build/test-classes build/test-dex out
VERSION_CODE="${VERSION_CODE:-101}"
VERSION_NAME="${VERSION_NAME:-1.0.1}"
KEYSTORE="${KEYSTORE:-${RUNNER_TEMP:-/tmp}/aura-development.keystore}"
KS_PASS="${KS_PASS:-android}"
KS_ALIAS="${KS_ALIAS:-aura}"
KEY_PASS="${KEY_PASS:-$KS_PASS}"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KEY_PASS" -alias "$KS_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Aura Launcher Development" >/dev/null 2>&1
  printf '%s\n' 'Development signing: configure the repository signing secrets before distributing production updates.'
fi
aapt2 compile --dir app/res -o build/resources.zip
aapt2 link -o build/base.apk -I "$ANDROID_JAR" --manifest app/AndroidManifest.xml -R build/resources.zip --auto-add-overlay --version-code "$VERSION_CODE" --version-name "$VERSION_NAME"
javac -encoding UTF-8 --release 11 -cp "$ANDROID_JAR" -d build/classes $(find app/src -name '*.java')
d8 --lib "$ANDROID_JAR" --min-api 26 --output build/dex $(find build/classes -name '*.class')
(cd build/dex && zip -q ../base.apk classes.dex)
zipalign -P 16 -f 4 build/base.apk build/aligned.apk
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KEY_PASS" --ks-key-alias "$KS_ALIAS" --out out/Aura-Launcher.apk build/aligned.apk
apksigner verify --verbose out/Aura-Launcher.apk
aapt2 dump badging out/Aura-Launcher.apk > out/apk-info.txt
mkdir -p build/model-tests
javac -d build/model-tests app/src/com/gr4nt/auralauncher/LayoutRules.java tests/LayoutRulesTest.java
java -cp build/model-tests LayoutRulesTest | tee out/layout-tests.txt
aapt2 link -o build/test.apk -I "$ANDROID_JAR" --manifest tests/AndroidManifest.xml
javac -encoding UTF-8 --release 11 -cp "$ANDROID_JAR:build/classes" -d build/test-classes tests/AuraSmoke.java
d8 --lib "$ANDROID_JAR" --classpath build/classes --min-api 26 --output build/test-dex $(find build/test-classes -name '*.class')
(cd build/test-dex && zip -q ../test.apk classes.dex)
zipalign -f 4 build/test.apk build/test-aligned.apk
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KEY_PASS" --ks-key-alias "$KS_ALIAS" --out build/Aura-tests.apk build/test-aligned.apk
sha256sum out/Aura-Launcher.apk > out/SHA256SUMS.txt
