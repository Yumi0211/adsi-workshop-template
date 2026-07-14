# Unit 09: Frontend 機能画面

## 概要

各画面の実装。Backend API が揃った後に結合する。

## 画面一覧

| 画面 | パス | ロール | 依存するBackend Unit |
|------|------|--------|---------------------|
| ダッシュボード | / | 全員 | unit_02, 03, 06 |
| 打刻 | /clock | 全員 | unit_02 |
| 月次勤怠一覧 | /attendance | 全員 | unit_03 |
| 有給残高・申請 | /leaves | 全員 | unit_04 |
| 打刻修正申請 | /corrections/new | 全員 | unit_05 |
| 自分の申請一覧 | /my-requests | 全員 | unit_05 |
| 承認一覧 | /approvals | 承認者 | unit_05 |
| 部門勤怠一覧 | /team/attendance | 承認者 | unit_03 |
| 社員管理 | /admin/employees | 管理者 | unit_01 |
| 部門管理 | /admin/departments | 管理者 | unit_01 |
| カレンダー設定 | /admin/calendars | 管理者 | unit_07 |
| レポート | /admin/reports | 管理者 | unit_07 |
| アラート一覧 | /admin/alerts | 管理者 | unit_06 |

## 依存

- unit_08_frontend_core（レイアウト・API Client・認証）
- Backend 各 Unit の API（結合時に必要）

## 実装順序（推奨）

Backend の完成順に合わせて段階的に実装:

1. 社員管理・部門管理（unit_01 完成後）
2. 打刻画面（unit_02 完成後）
3. 月次勤怠・有給（unit_03, 04 完成後）
4. 承認・申請系（unit_05 完成後）
5. アラート・レポート・ダッシュボード（unit_06, 07 完成後）

## 完了条件

- [ ] 全13画面が動作する
- [ ] レスポンシブ対応（モバイル/デスクトップ）
- [ ] ロール別のアクセス制御が画面レベルで機能する
- [ ] API エラー時にユーザーフレンドリーなメッセージが表示される
