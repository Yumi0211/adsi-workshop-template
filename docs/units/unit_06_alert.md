# Unit 06: アラートドメイン

## 概要

法令準拠のためのアラート生成・通知。36協定、有給5日義務、勤務間インターバル。

## ユーザーストーリー

- 社員として、残業が上限に近づいたら気づきたい
- 社員として、勤務間インターバル違反を確認したい
- 管理者として、36協定超過リスクのある社員を把握したい
- 管理者として、有給5日未取得の社員を把握したい

## テーブル

- alerts

## API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/alerts/my | 自分のアラート |
| GET | /api/v1/admin/alerts | 全社アラート一覧（管理者） |
| PUT | /api/v1/admin/alerts/{id}/acknowledge | アラート確認済み |

## Entity

- Alert

## Service

- OvertimeAlertService（36協定チェック: 月45h / 年360h）
- IntervalCheckService（勤務間インターバル 11h チェック）
- LeaveObligationAlertService（年5日有給取得義務チェック）

## ドメインルール

- 月45h超過 → OVERTIME_MONTHLY アラート生成
- 年360h超過見込み → OVERTIME_YEARLY アラート生成
- 前日退勤〜当日出勤 < 11h → INTERVAL_VIOLATION アラート生成
- 年度内に5日未取得（残り3ヶ月時点で0-2日） → LEAVE_OBLIGATION アラート生成
- 同一条件のアラートは重複生成しない

## 依存

- unit_01_employee（対象社員）
- unit_03_attendance（残業時間集計、退勤・出勤時刻）
- unit_04_leave（有給取得日数）

## 完了条件

- [ ] 残業月45h超過でアラートが生成される
- [ ] 勤務間インターバル違反が検知される
- [ ] 有給5日未取得アラートが生成される
- [ ] 重複アラートが生成されない
- [ ] 管理者がアラートを確認済みにできる
