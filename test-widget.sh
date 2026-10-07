#!/bin/sh
set -eu

printf '%s\n' "==> Running Go unit tests..."
go test -v ./disco-backend/engine/...

printf '%s\n' "==> Building widget bundle..."
./build-widget.sh

printf '%s\n' "==> Verifying bundle contents..."
test -s dist/widget.js   || { echo "ERROR: dist/widget.js is empty"; exit 1; }
test -s dist/widget.json || { echo "ERROR: dist/widget.json is empty"; exit 1; }

printf '%s\n' "==> All checks passed. Widget bundle is at dist/ and engine tests are green."
