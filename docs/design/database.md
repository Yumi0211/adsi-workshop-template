# DB設計

## 方針

- Flyway でマイグレーション管理（`ddl-auto` 禁止）
- 楽観ロック（`version` カラム）を全主要テーブルに付与
- 論理削除は使わず、`active` フラグまたは `end_date` で無効化
- タイムスタンプは `TIMESTAMP WITH TIME ZONE`（Asia/Tokyo）

---

## テーブル定義

### employees（社員）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_code | VARCHAR(20) | UNIQUE NOT NULL | 社員番号 |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | UNIQUE NOT NULL | メール（Entra ID キー） |
| role | VARCHAR(20) | NOT NULL | EMPLOYEE/APPROVER/ADMIN |
| hire_date | DATE | NOT NULL | 入社日 |
| active | BOOLEAN | NOT NULL DEFAULT TRUE | 有効フラグ |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

### departments（部門）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| name | VARCHAR(100) | NOT NULL | 部門名 |
| level | VARCHAR(20) | NOT NULL | HEADQUARTERS/DIVISION/SECTION |
| parent_id | BIGINT | FK → departments(id), NULL可 | 親部門 |
| active | BOOLEAN | NOT NULL DEFAULT TRUE | |
| version | BIGINT | NOT NULL DEFAULT 0 | |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

### employee_departments（社員部門所属）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| department_id | BIGINT | FK → departments(id), NOT NULL | |
| is_primary | BOOLEAN | NOT NULL DEFAULT FALSE | 主所属 |
| start_date | DATE | NOT NULL | 所属開始日 |
| end_date | DATE | NULL | 所属終了日 |

- UNIQUE(employee_id, department_id, start_date)

### time_records（打刻）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| record_date | DATE | NOT NULL | 打刻日 |
| type | VARCHAR(20) | NOT NULL | CLOCK_IN/CLOCK_OUT/BREAK_START/BREAK_END |
| recorded_at | TIMESTAMPTZ | NOT NULL | 打刻時刻 |
| source | VARCHAR(10) | NOT NULL | IC_CARD/WEB |
| version | BIGINT | NOT NULL DEFAULT 0 | |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

- INDEX(employee_id, record_date)

### daily_attendances（日次勤怠）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| attendance_date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMPTZ | NULL | 出勤時刻 |
| clock_out | TIMESTAMPTZ | NULL | 退勤時刻 |
| break_minutes | INTEGER | NOT NULL DEFAULT 0 | 休憩（分） |
| working_minutes | INTEGER | NOT NULL DEFAULT 0 | 勤務（分） |
| overtime_minutes | INTEGER | NOT NULL DEFAULT 0 | 残業（分） |
| night_minutes | INTEGER | NOT NULL DEFAULT 0 | 深夜（分） |
| is_holiday_work | BOOLEAN | NOT NULL DEFAULT FALSE | 休日勤務 |
| status | VARCHAR(20) | NOT NULL | PRESENT/ABSENT/HOLIDAY/PAID_LEAVE/HALF_LEAVE_AM/HALF_LEAVE_PM |
| version | BIGINT | NOT NULL DEFAULT 0 | |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

- UNIQUE(employee_id, attendance_date)
- INDEX(employee_id, attendance_date)

### leave_balances（有給残高）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| fiscal_year | INTEGER | NOT NULL | 年度 |
| granted_days | DECIMAL(4,1) | NOT NULL | 付与日数 |
| used_days | DECIMAL(4,1) | NOT NULL DEFAULT 0 | 使用日数 |
| carried_over_days | DECIMAL(4,1) | NOT NULL DEFAULT 0 | 繰越日数 |
| grant_date | DATE | NOT NULL | 付与日 |
| expiry_date | DATE | NOT NULL | 失効日 |
| version | BIGINT | NOT NULL DEFAULT 0 | |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

- UNIQUE(employee_id, fiscal_year, grant_date)

### approval_requests（申請）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| applicant_id | BIGINT | FK → employees(id), NOT NULL | 申請者 |
| approver_id | BIGINT | FK → employees(id), NOT NULL | 承認者 |
| type | VARCHAR(20) | NOT NULL | TIME_CORRECTION/LEAVE/OVERTIME |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| request_date | DATE | NOT NULL | 申請日 |
| detail | JSONB | NOT NULL | 申請内容 |
| reason | TEXT | NULL | 申請理由 |
| approved_at | TIMESTAMPTZ | NULL | 承認日時 |
| rejection_reason | TEXT | NULL | 却下理由 |
| version | BIGINT | NOT NULL DEFAULT 0 | |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

- INDEX(approver_id, status)
- INDEX(applicant_id)

#### detail JSONB の構造

**TIME_CORRECTION:**
```json
{
  "targetDate": "2026-07-14",
  "originalClockIn": "2026-07-14T09:00:00",
  "correctedClockIn": "2026-07-14T08:30:00",
  "originalClockOut": null,
  "correctedClockOut": null
}
```

**LEAVE:**
```json
{
  "leaveDate": "2026-07-20",
  "leaveType": "FULL | HALF_AM | HALF_PM"
}
```

**OVERTIME:**
```json
{
  "targetMonth": "2026-07",
  "currentOvertimeMinutes": 2700,
  "expectedOvertimeMinutes": 3000
}
```

### work_calendars（勤務カレンダー）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| calendar_date | DATE | UNIQUE NOT NULL | 日付 |
| day_type | VARCHAR(20) | NOT NULL | WORKDAY/HOLIDAY/COMPANY_HOLIDAY |
| description | VARCHAR(100) | NULL | 祝日名等 |
| fiscal_year | INTEGER | NOT NULL | 年度 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

- INDEX(fiscal_year)

### alerts（アラート）

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 対象社員 |
| type | VARCHAR(30) | NOT NULL | OVERTIME_MONTHLY/OVERTIME_YEARLY/LEAVE_OBLIGATION/INTERVAL_VIOLATION |
| message | TEXT | NOT NULL | アラートメッセージ |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| acknowledged | BOOLEAN | NOT NULL DEFAULT FALSE | 確認済み |

- INDEX(employee_id, acknowledged)
- INDEX(type, acknowledged)

---

## ER図（テキスト）

```
employees ─────────────────┐
  │                        │
  │ 1:N                    │ N:M (via employee_departments)
  ▼                        ▼
time_records          departments
daily_attendances       │ self-ref (parent_id)
leave_balances          │
approval_requests       │
alerts                  │
                        ▼
                  employee_departments

work_calendars (独立)
```

---

## マイグレーション計画

| バージョン | ファイル | 内容 |
|-----------|---------|------|
| V1 | V1__create_employees.sql | employees テーブル |
| V2 | V2__create_departments.sql | departments + employee_departments |
| V3 | V3__create_time_records.sql | time_records |
| V4 | V4__create_daily_attendances.sql | daily_attendances |
| V5 | V5__create_leave_balances.sql | leave_balances |
| V6 | V6__create_approval_requests.sql | approval_requests |
| V7 | V7__create_work_calendars.sql | work_calendars |
| V8 | V8__create_alerts.sql | alerts |
