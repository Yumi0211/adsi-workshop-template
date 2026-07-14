# 開発環境

## 前提条件

- Java 21
- Gradle 8.11+（Wrapper 同梱）
- Node.js 18+（npm スクリプト用）

## 初回セットアップ

```bash
npm run setup    # backend ビルド（依存ダウンロード）
```

## 起動・停止

| コマンド | 説明 |
|---------|------|
| `npm run boot` | Backend 起動（H2 インメモリ DB、認証: dev/dev） |
| `npm run boot:stop` | Backend 停止 |
| `npm run check:backend` | テスト実行 |
| `npm run clean` | ビルド成果物削除 |

## SageMaker Code Editor でのアクセス

### 1. 起動

```bash
npm run boot
```

### 2. ブラウザで開く

1. **PORTS タブ**で 8080 の地球儀ボタンを押す
2. 開いた URL の **`ports` を `absports` に置換**して Enter

```
# 変換前
https://<studio-domain>/codeeditor/default/ports/8080/absports/8080/actuator/health

# 正しいURL
https://<studio-domain>/codeeditor/default/absports/8080/absports/8080/actuator/health
```

### 3. Basic 認証

| 項目 | 値 |
|------|---|
| ユーザー名 | `dev` |
| パスワード | `dev` |

### 4. 確認できるエンドポイント

| パス | 説明 |
|------|------|
| `/absports/8080/actuator/health` | ヘルスチェック（認証不要） |
| `/absports/8080/api/v1/auth/me` | ログインユーザー情報（要認証） |

## プロファイル

| プロファイル | 用途 | DB | 認証 |
|------------|------|---|------|
| `local` | 開発（SageMaker含む） | H2 インメモリ | Basic (dev/dev) |
| `test` | テスト | H2 インメモリ | Mock |
| (デフォルト) | 本番 | PostgreSQL | Entra ID SSO |

## context-path について

`local` プロファイルでは `server.servlet.context-path=/absports/8080` が設定されています。
これは SageMaker Code Editor のプロキシがこのパスプレフィックスを付けてリクエストを転送するためです。

- SageMaker 上: ブラウザから自然にアクセス可能
- ターミナルから: `curl http://localhost:8080/absports/8080/actuator/health`

## トラブルシューティング

### ポートが使用中

```bash
npm run boot:stop
# または
pkill -f java
```

### ECONNREFUSED

アプリが起動していません。`npm run boot` を実行してください。
起動には約10-15秒かかります。

### 認証ダイアログが出る

`dev` / `dev` を入力してください（`application-local.yml` で設定した開発用認証情報）。
