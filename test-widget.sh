#!/bin/sh
set -eu

go test ./...
./build-widget.sh
test -s dist/widget.js
test -s dist/widget.json
printf '%s\n' "Widget build and Go tests passed."
