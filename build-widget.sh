#!/bin/sh
set -eu

rm -rf dist
mkdir -p dist
cp src/widget.js dist/widget.js
cp widget.json dist/widget.json
cp -R disco-backend/assets dist/assets
printf '%s\n' "Widget bundle created at $(pwd)/dist"
