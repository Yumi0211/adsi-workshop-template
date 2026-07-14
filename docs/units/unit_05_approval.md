# Unit 05: 申請・承認ドメイン

## 概要

打刻修正・有給・残業超過の申請と承認ワークフロー。

## ユーザーストーリー

- 社員として、打刻修正を申請したい
- 社員として、自分の申請状態を確認したい
- 承認者として、未処理の申請一覧を確認したい
- 承認者として、申請を承認・却下したい

## テーブル

- approval_requests

## API

| Method | Path | 説明 |
|--------|------|------|
| POST | /api/v1/time-corrections | 打刻修正申請 |
| GET | /api/v1/approvals/pending | 未処理申請一覧（承認者） |
| GET | /api/v1/approvals/{id} | 申請詳細 |
| PUT | /api/v1/approvals/{id}/approve | 承認 |
| PUT | /api/v1/approvals/{id}/reject | 却下 |
| GET | /api/v1/my-requests | 自分の申請一覧 |

## Entity

- ApprovalRequest

## Service

- ApprovalService（申請作成・承認・却下・副作用の実行）

## ドメインルール

- 承認者は申請者の所属部門の承認者ロール保持者
- 承認時の副作用:
  - TIME_CORRECTION → TimeRecord を修正、DailyAttendance を再計算
  - LEAVE → LeaveBalance の used_days を加算
  - OVERTIME → アラートを解除
- 却下時は理由必須
- 自分自身の申請は承認できない
- PENDING 以外のステータスは変更不可

## 依存

- unit_01_employee（承認者の特定、部門関係）
- unit_02_time_record（打刻修正の副作用）
- unit_04_leave（有給消化の副作用）

## 完了条件

- [ ] 打刻修正申請が作成できる
- [ ] 承認者が未処理一覧を取得できる
- [ ] 承認すると打刻が修正される
- [ ] 有給承認で残日数が減る
- [ ] 却下理由が保存される
- [ ] 権限チェックが機能する
