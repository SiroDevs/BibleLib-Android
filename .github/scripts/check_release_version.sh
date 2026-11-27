#!/usr/bin/env bash
set -euo pipefail

version_name=$(sed -nE 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' app/build.gradle.kts | head -n1)
version_code=$(sed -nE 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' app/build.gradle.kts | head -n1)

if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "::error::Expected versionName in x.y.z form; got '$version_name'."
  exit 1
fi
if ! [[ "$version_code" =~ ^[1-9][0-9]*$ ]]; then
  echo "::error::Expected a positive versionCode; got '$version_code'."
  exit 1
fi

latest_tag=$(gh release list --repo "$GITHUB_REPOSITORY" --limit 200 --exclude-drafts \
  --json tagName --jq '.[].tagName' | sort -V | tail -n1)
latest_version=${latest_tag#v}
should_release=true

if [ -n "$latest_tag" ]; then
  newest_version=$(printf '%s\n%s\n' "$latest_version" "$version_name" | sort -V | tail -n1)
  if [ "$version_name" = "$latest_version" ] || [ "$newest_version" != "$version_name" ]; then
    should_release=false
  else
    latest_assets="$RUNNER_TEMP/latest-release"
    mkdir -p "$latest_assets"
    if gh release download "$latest_tag" --repo "$GITHUB_REPOSITORY" \
        --pattern 'BibleLib_*.apk.info.txt' --dir "$latest_assets" >/dev/null 2>&1; then
      latest_code=$(find "$latest_assets" -type f -name 'BibleLib_*.apk.info.txt' \
        -exec awk -F= '$1 == "versionCode" {print $2}' {} + | sort -n | tail -n1)
    fi
    if [ -z "${latest_code:-}" ]; then
      latest_code=${latest_version##*.}
    fi
    if [ "$version_code" -le "$latest_code" ]; then
      echo "::error::versionCode $version_code must exceed the last release's $latest_code."
      exit 1
    fi
  fi
fi

echo "version_name=$version_name" >> "$GITHUB_OUTPUT"
echo "version_code=$version_code" >> "$GITHUB_OUTPUT"
echo "should_release=$should_release" >> "$GITHUB_OUTPUT"
{
  echo "### BibleLib release version"
  echo "- Candidate: $version_name (versionCode $version_code)"
  echo "- Latest GitHub release: ${latest_tag:-none}"
  if [ "$should_release" = true ]; then
    echo "- Result: building and publishing this release"
  else
    echo "- Result: skipped; the version is not newer than the latest release"
  fi
} >> "$GITHUB_STEP_SUMMARY"
