# Unit of Work — 2人並行実装プラン

## 方針

- **Unit 00（共通基盤）を最初に1人で完成させる**（テスト実行環境込み）
- 以降は **2人で並行実装**（Backend担当 / Frontend担当、または機能別に分担）

---

## Unit 一覧

| Unit | ドメイン | 説明 |
|------|---------|------|
| unit_00_foundation | 共通基盤 | プロジェクト骨格・DB接続・認証・テスト基盤 |
| unit_01_employee | 社員・組織 | 社員マスタ・部門マスタ・兼務管理 |
| unit_02_time_record | 打刻 | ICカード/Web打刻 |
| unit_03_attendance | 勤怠計算 | 日次勤怠計算・月次集計 |
| unit_04_leave | 有給休暇 | 有給付与・残高・申請 |
| unit_05_approval | 申請・承認 | 打刻修正・有給・残業のワークフロー |
| unit_06_alert | アラート | 36協定・有給義務・インターバル |
| unit_07_admin | 管理者機能 | カレンダー・レポート |
| unit_08_frontend_core | Frontend基盤 | Next.js骨格・認証・API Client |
| unit_09_frontend_features | Frontend機能 | 全13画面 |

---

## 依存図

```
                    unit_00_foundation
                    （共通基盤・テスト環境）
                           │
              ┌────────────┼────────────┐
              ▼                         ▼
      unit_01_employee          unit_08_frontend_core
              │                         │
       ┌──────┴──────┐                  │
       ▼             ▼                  │
unit_02_time_record  unit_04_leave      │
       │             │                  │
       ▼             ▼                  │
unit_03_attendance   unit_05_approval   │
       │             │                  │
       └──────┬──────┘                  │
              ▼                         │
        unit_06_alert                   │
              │                         │
              ▼                         ▼
        unit_07_admin           unit_09_frontend_features
```

---

## 2人並行実装の Phase 計画

### Phase 0: 共通基盤（1人 or ペア、1-2日）

| 担当 | Unit | 内容 |
|------|------|------|
| 共同 | unit_00_foundation | Spring Boot/Next.js プロジェクト生成、Gradle/npm 設定、DB接続、Flyway、Testcontainers、`./gradlew test` が通る状態 |

**Phase 0 完了条件**: `./gradlew test` と `npm test` が空テストで通る。Flyway マイグレーション実行。SSO 設定。

---

### Phase 1: 基盤 Entity + Frontend 骨格（並行）

| 担当A（Backend） | 担当B（Frontend） |
|-----------------|------------------|
| unit_01_employee | unit_08_frontend_core |
| 社員・部門 CRUD の TDD 実装 | Next.js レイアウト・認証・API Client |

---

### Phase 2: コアドメイン（並行）

| 担当A（Backend） | 担当B（Backend or Frontend） |
|-----------------|---------------------------|
| unit_02_time_record | unit_04_leave |
| 打刻の TDD 実装 | 有給休暇の TDD 実装 |

---

### Phase 3: 計算・ワークフロー（並行）

| 担当A | 担当B |
|-------|-------|
| unit_03_attendance | unit_05_approval |
| 勤怠計算の TDD 実装 | 承認ワークフローの TDD 実装 |

---

### Phase 4: アラート + 管理者（並行）

| 担当A | 担当B |
|-------|-------|
| unit_06_alert | unit_07_admin |
| アラート生成 | カレンダー・レポート |

---

### Phase 5: Frontend 画面（並行）

| 担当A | 担当B |
|-------|-------|
| unit_09 の一般社員画面 | unit_09 の承認者・管理者画面 |
| ダッシュボード、打刻、勤怠一覧、有給、申請 | 承認一覧、部門勤怠、社員管理、レポート、アラート |

---

### Phase 6: 統合テスト（共同）

- 全 Unit が揃った状態で E2E テスト
- 画面遷移・権限・データフローの通し確認

---

## タイムライン目安

| Phase | 期間目安 | 備考 |
|-------|---------|------|
| 0 | 1-2日 | プロジェクト骨格 |
| 1 | 2-3日 | 基盤テーブル＋Frontend骨格 |
| 2 | 2-3日 | 打刻＋有給 |
| 3 | 3-4日 | 計算ロジック＋承認フロー |
| 4 | 2-3日 | アラート＋管理者 |
| 5 | 3-5日 | 全画面実装 |
| 6 | 2-3日 | 統合テスト・バグ修正 |
| **合計** | **約15-23日** | 2人並行 |

---

## コンフリクト回避ルール

- **パッケージ境界で分割**: 各 Unit は独自パッケージ内で完結
- **共通テーブルの FK**: Flyway マイグレーションは番号順で排他（Phase 0 で全テーブル作成推奨）
- **API パスで分割**: `/api/v1/time-records` と `/api/v1/leaves` は別ファイル
- **Git ブランチ**: Unit ごとにブランチを切り、main へ PR マージ
