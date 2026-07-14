# Unit 03: 勤怠計算ドメイン

## 概要

打刻データから日次勤怠（勤務時間・残業・深夜・休日勤務）を計算し、月次集計を提供する。

## ユーザーストーリー

- 社員として、自分の月次勤怠一覧を確認したい
- 社員として、月の合計勤務時間・残業時間を確認したい
- 承認者として、部門メンバーの月次勤怠を確認したい

## テーブル

- daily_attendances

## API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/attendances/monthly?year={}&month={} | 自分の月次勤怠 |
| GET | /api/v1/attendances/monthly/summary?year={}&month={} | 月次サマリー |
| GET | /api/v1/departments/{id}/attendances/monthly?year={}&month={} | 部門月次勤怠 |

## Entity

- DailyAttendance

## Value Object

- WorkDuration（勤務時間、分単位）
- TimeRange（時間範囲、重複計算）

## Service

- AttendanceCalculationService（日次勤怠計算）
- MonthlyReportService（月次集計）

## ドメインルール

- 勤務時間 = 退勤 − 出勤 − 休憩
- 残業 = 勤務時間 − 7.5h（正の場合のみ）
- 深夜 = 22:00〜05:00 と勤務時間の重複部分
- 休日勤務 = WorkCalendar で HOLIDAY/COMPANY_HOLIDAY の日の勤務
- 退勤未打刻の場合は計算保留（status = PRESENT、時間は 0）

## 依存

- unit_01_employee（Employee FK）
- unit_02_time_record（TimeRecord データ参照）
- unit_00_foundation（WorkCalendar 参照 — カレンダーは unit_07 で管理画面を作るが、テーブル自体は Phase A で作成）

## 完了条件

- [ ] 打刻データから日次勤怠が正しく計算される
- [ ] 残業時間が正しく算出される
- [ ] 深夜勤務時間が自動判定される
- [ ] 休日勤務が判定される
- [ ] 月次集計 API が動作する
- [ ] 承認者が部門メンバーの勤怠を取得できる
