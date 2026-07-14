# Unit 01: 社員・組織ドメイン

## 概要

社員マスタと部門マスタの CRUD。他 Unit の FK 先となるため早期に実装する。

## ユーザーストーリー

- 管理者として、社員を登録・編集・無効化したい
- 管理者として、部門（本部/部/課）を登録・編集したい
- 管理者として、社員を部門に所属させたい（兼務対応）
- 管理者として、社員にロール（一般/承認者/管理者）を割り当てたい

## テーブル

- employees
- departments
- employee_departments

## API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/admin/employees | 社員一覧 |
| GET | /api/v1/admin/employees/{id} | 社員詳細 |
| POST | /api/v1/admin/employees | 社員登録 |
| PUT | /api/v1/admin/employees/{id} | 社員編集 |
| PUT | /api/v1/admin/employees/{id}/deactivate | 社員無効化 |
| GET | /api/v1/admin/departments | 部門ツリー |
| POST | /api/v1/admin/departments | 部門登録 |
| PUT | /api/v1/admin/departments/{id} | 部門編集 |
| PUT | /api/v1/admin/departments/{id}/deactivate | 部門無効化 |

## Entity

- Employee
- Department
- EmployeeDepartment

## Service

- EmployeeService（社員 CRUD、ロール管理）
- DepartmentService（部門 CRUD、階層管理、配下社員取得）

## ドメインルール

- 社員番号は一意
- メールアドレスは一意（Entra ID 連携キー）
- 主所属は1人につき必ず1つ
- 部門の無効化は配下社員がいない場合のみ可能

## 依存

- unit_00_foundation（共通 Enum、SecurityFilterChain）

## 完了条件

- [ ] 社員 CRUD の API が動作する
- [ ] 部門ツリーが正しく取得できる
- [ ] 兼務（複数部門所属）が登録できる
- [ ] 楽観ロックが機能する
- [ ] 管理者ロール以外は 403 が返る
