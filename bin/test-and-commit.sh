#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: bin/test-and-commit.sh \"<commit message>\"" >&2
  exit 1
fi

msg="$1"

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

sbt fix fmt testQuick stage && git commit -a -m "$msg"
