#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
docker compose up -d --wait db
cd backend
exec mvn compile exec:java
