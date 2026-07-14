# Unit 07: 管理者機能

## 概要

管理者向けのマスタ管理画面（カレンダー設定）とレポート。
社員・部門マスタは unit_01 で API 実装済み。ここではカレンダーとレポートを追加する。

## ユーザーストーリー

- 管理者として、勤務カレンダー（祝日・会社休日）を設定したい
- 管理者として、全社の月次残業レポートを確認したい
- 管理者として、有給5日未取得者一覧を確認したい
- 管理者として、36協定超過者一覧を確認したい

## テーブル

- work_calendars

## API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/admin/calendars?fiscalYear={} | カレンダー取得 |
| POST | /api/v1/admin/calendars | 休日登録 |
| PUT | /api/v1/admin/calendars/{id} | 休日編集 |
| DELETE | /api/v1/admin/calendars/{id} | 休日削除 |
| GET | /api/v1/admin/reports/overtime?year={}&month={} | 残業レポート |
| GET | /api/v1/admin/reports/leave-obligation?fiscalYear={} | 有給未取得一覧 |

## Entity

- WorkCalendar

## Service

- WorkCalendarService（カレンダー CRUD、休日判定）
- MonthlyReportService（残業レポート集計 — unit_03 から拡張）

## 依存

- unit_01_employee（社員・部門情報）
- unit_03_attendance（残業集計データ）
- unit_04_leave（有給取得状況）
- unit_06_alert（アラート一覧）

## 完了条件

- [ ] 勤務カレンダーの CRUD が動作する
- [ ] 部門別残業レポートが取得できる
- [ ] 有給5日未取得者一覧が取得できる
- [ ] 管理者ロール以外は 403
