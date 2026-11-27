#!/usr/bin/env bash
set -euo pipefail

for secret_name in ANDROID_KEYSTORE_BASE64 ANDROID_KEYSTORE_PASSWORD ANDROID_KEY_ALIAS ANDROID_KEY_PASSWORD; do
  if [ -z "${!secret_name:-}" ]; then
    echo "::error::$secret_name is missing from GitHub Actions secrets."
    exit 1
  fi
done

mkdir -p app/keystore
printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 --decode > app/keystore/release.jks
chmod 600 app/keystore/release.jks
cat > app/keystore/key.properties <<EOF
KEY_ALIAS=$ANDROID_KEY_ALIAS
KEY_PASSWORD=$ANDROID_KEY_PASSWORD
KEYSTORE_PASSWORD=$ANDROID_KEYSTORE_PASSWORD
STORE_FILE=release.jks
EOF
chmod 600 app/keystore/key.properties

expected_sha256=$(keytool -list -v -keystore app/keystore/release.jks \
  -alias "$ANDROID_KEY_ALIAS" -storepass:env ANDROID_KEYSTORE_PASSWORD 2>/dev/null \
  | sed -n 's/^[[:space:]]*SHA256:[[:space:]]*//p' \
  | head -n1 | tr -d ':' | tr 'A-F' 'a-f')
if [ -z "$expected_sha256" ]; then
  echo "::error::Could not open the configured keystore alias. Check the signing secrets."
  exit 1
fi
echo "expected_sha256=$expected_sha256" >> "$GITHUB_OUTPUT"
