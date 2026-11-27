#!/usr/bin/env bash
set -euo pipefail

pr_number=$(gh api "repos/$GITHUB_REPOSITORY/commits/$GITHUB_SHA/pulls" \
  --jq '[.[] | select(.merged_at != null)] | sort_by(.merged_at) | last | .number // empty' || true)
if [ -z "${pr_number:-}" ]; then
  pr_number=$(gh pr list --repo "$GITHUB_REPOSITORY" --state merged --base main \
    --limit 1 --json number --jq '.[0].number // empty' || true)
fi

{
  echo "## BibleLib v$VERSION_NAME"
  echo
  if [ -n "${pr_number:-}" ]; then
    gh pr view "$pr_number" --repo "$GITHUB_REPOSITORY" --json title,body,url \
      --jq '"### " + .title + "\n\n" + (.body // "") + "\n\n" + .url'
  else
    echo "BibleLib v$VERSION_NAME Android release."
  fi
} > release-notes.md
