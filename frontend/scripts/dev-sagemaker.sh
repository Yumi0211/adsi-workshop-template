#!/bin/bash
set -e

cd "$(dirname "$0")/.."

# 既存プロセス停止
pkill -f "next dev\|next start" 2>/dev/null || true
pkill -f "sagemaker-proxy" 2>/dev/null || true
sleep 1

# 環境変数
export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"
export NEXTAUTH_SECRET="${NEXTAUTH_SECRET:-dev-secret}"

# Next.js 起動（dev モード）
npx next dev -p 3001 &
NEXT_PID=$!

# Next.js が起動するまで待機
echo "Waiting for Next.js to start..."
for i in $(seq 1 30); do
  if curl -s -o /dev/null http://127.0.0.1:3001; then
    break
  fi
  sleep 1
done

# 復元プロキシ起動
node scripts/sagemaker-proxy.mjs &
PROXY_PID=$!

echo ""
echo "=== SageMaker Preview Ready ==="
echo "PORTS タブで 3000 の地球儀を押し、URL の 'ports' を 'absports' に置換"
echo ""
echo "Next.js PID: $NEXT_PID"
echo "Proxy PID:   $PROXY_PID"

wait
