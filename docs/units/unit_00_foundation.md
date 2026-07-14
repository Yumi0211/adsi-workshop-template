# Unit 00: 共通基盤

## 概要

プロジェクトの骨格と全 Unit が依存する共通部品を構築する。

## スコープ

### Backend

- Spring Boot プロジェクト生成（Gradle）
- パッケージ構成の確立
- `application.yml`（DB接続、Spring Security OAuth2 設定）
- 共通 Enum（Role, DepartmentLevel, TimeRecordType, RecordSource, AttendanceStatus, RequestType, RequestStatus, DayType, AlertType）
- 共通例外クラス + `@RestControllerAdvice`（RFC 7807）
- SecurityFilterChain Bean（Entra ID OIDC 設定）
- CORS 設定
- 監査用 `AuditorAware` 設定
- ヘルスチェックエンドポイント

### Frontend

- （unit_08 で別途実施）

### DB

- Flyway 設定（`flyway.yml`）
- テスト用プロファイル（H2 or Testcontainers）

## テーブル

なし（共通設定のみ）

## API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/auth/me | ログインユーザー情報 |
| POST | /api/v1/auth/logout | ログアウト |
| GET | /actuator/health | ヘルスチェック |

## 依存

なし（最初に実装する）

## 完了条件

- [ ] `./gradlew build` が通る
- [ ] Flyway が起動時に実行される
- [ ] `/actuator/health` が 200 を返す
- [ ] SSO ログイン → `/api/v1/auth/me` でユーザー情報が取れる
- [ ] 不正リクエストに RFC 7807 形式のエラーが返る
