#!/usr/bin/env bash
set -euo pipefail

apk_source=app/build/outputs/apk/release/app-release.apk
aab_source=app/build/outputs/bundle/release/app-release.aab
if [ ! -f "$apk_source" ] || [ ! -f "$aab_source" ]; then
  echo "::error::Expected release APK or AAB was not produced."
  find app/build/outputs -type f \( -name '*.apk' -o -name '*.aab' \) -print || true
  exit 1
fi

mkdir -p out
cp "$apk_source" "out/BibleLib_${VERSION_NAME}.apk"
cp "$aab_source" "out/BibleLib_${VERSION_NAME}.aab"

build_tools=$(ls -d "$ANDROID_HOME"/build-tools/* | sort -V | tail -n1)
"$build_tools/apksigner" verify --verbose --print-certs "out/BibleLib_${VERSION_NAME}.apk" > apksigner.txt
apk_sha256=$(grep -m1 'certificate SHA-256 digest' apksigner.txt | awk '{print $NF}' | tr -d ':' | tr 'A-F' 'a-f')
aab_sha256=$(keytool -printcert -jarfile "out/BibleLib_${VERSION_NAME}.aab" \
  | sed -n 's/^[[:space:]]*SHA256:[[:space:]]*//p' \
  | head -n1 | tr -d ':' | tr 'A-F' 'a-f')
if [ "$apk_sha256" != "$EXPECTED_SHA256" ] || [ "$aab_sha256" != "$EXPECTED_SHA256" ]; then
  echo "::error::The release APK or AAB is not signed with the configured upload key."
  exit 1
fi
jarsigner -verify "out/BibleLib_${VERSION_NAME}.aab" >/dev/null

badging=$("$build_tools/aapt2" dump badging "out/BibleLib_${VERSION_NAME}.apk" | grep -m1 '^package:')
package_name=$(sed -nE "s/^package: name='([^']*)'.*/\1/p" <<< "$badging")
apk_version=$(sed -nE "s/.* versionName='([^']*)'.*/\1/p" <<< "$badging")
apk_code=$(sed -nE "s/.* versionCode='([^']*)'.*/\1/p" <<< "$badging")
if [ "$package_name" != 'com.biblelib' ] || [ "$apk_version" != "$VERSION_NAME" ] || [ "$apk_code" != "$VERSION_CODE" ]; then
  echo "::error::APK metadata does not match app/build.gradle.kts."
  echo "Found $package_name $apk_version ($apk_code)."
  exit 1
fi

{
  echo "package=$package_name"
  echo "versionName=$apk_version"
  echo "versionCode=$apk_code"
  echo "signingCertSha256=$apk_sha256"
} > "out/BibleLib_${VERSION_NAME}.apk.info.txt"
echo "Verified BibleLib $apk_version ($apk_code), signed with $apk_sha256." >> "$GITHUB_STEP_SUMMARY"
