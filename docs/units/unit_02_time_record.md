# Unit 02: 打刻ドメイン

## 概要

ICカード / Web による出退勤・休憩の打刻記録。

## ユーザーストーリー

- 社員として、出勤時にICカードで打刻したい
- 社員として、ICカード忘れ時にWebから打刻したい
- 社員として、休憩開始・終了を打刻したい
- 社員として、今日の打刻履歴を確認したい

## テーブル

- time_records

## API

| Method | Path | 説明 |
|--------|------|------|
| POST | /api/v1/time-records | 打刻記録 |
| GET | /api/v1/time-records?date={date} | 指定日の打刻一覧 |

## Entity

- TimeRecord

## Service

- TimeRecordService（打刻記録・バリデーション）

## ドメインルール

- 打刻順序: CLOCK_IN → (BREAK_START → BREAK_END)* → CLOCK_OUT
- 同日に CLOCK_IN が2回記録されない（二重打刻防止）
- CLOCK_OUT 済みの日に追加打刻はできない
- サーバー時刻を正とする

## 依存

- unit_01_employee（Employee FK）

## 完了条件

- [ ] Web 打刻（出勤/退勤/休憩開始/休憩終了）が記録できる
- [ ] ICカード打刻のエンドポイントが動作する
- [ ] 打刻順序バリデーションが機能する
- [ ] 二重打刻が防止される
