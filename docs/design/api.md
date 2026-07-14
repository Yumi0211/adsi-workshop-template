# API設計

## 方針

- RESTful API（JSON）
- 認証: Microsoft Entra ID SSO（OAuth2 Bearer Token）
- パス: `/api/v1/...`
- エラーレスポンス: RFC 7807 Problem Details 形式
- ページネーション: `page` / `size` パラメータ（デフォルト size=20）

---

## エンドポイント一覧

### 打刻 API

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| POST | /api/v1/time-records | 打刻を記録 | 全員 |
| GET | /api/v1/time-records?date={date} | 指定日の打刻一覧取得 | 全員（自分のみ） |

#### POST /api/v1/time-records

```yaml
Request:
  type: "CLOCK_IN" | "CLOCK_OUT" | "BREAK_START" | "BREAK_END"
  source: "IC_CARD" | "WEB"
  cardId: string  # IC_CARD の場合のみ

Response: 201
  id: number
  employeeId: number
  recordDate: string (date)
  type: string
  recordedAt: string (datetime)
  source: string
```

---

### 勤怠 API

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/attendances/monthly?year={}&month={} | 自分の月次勤怠 | 全員 |
| GET | /api/v1/attendances/monthly/summary?year={}&month={} | 月次サマリー | 全員 |
| GET | /api/v1/departments/{id}/attendances/monthly?year={}&month={} | 部門メンバー月次勤怠 | 承認者/管理者 |

#### GET /api/v1/attendances/monthly

```yaml
Response: 200
  employeeId: number
  year: number
  month: number
  records:
    - date: string (date)
      clockIn: string (datetime) | null
      clockOut: string (datetime) | null
      breakMinutes: number
      workingMinutes: number
      overtimeMinutes: number
      nightMinutes: number
      isHolidayWork: boolean
      status: string
  summary:
    totalWorkingMinutes: number
    totalOvertimeMinutes: number
    totalNightMinutes: number
    totalHolidayWorkMinutes: number
    workingDays: number
```

---

### 有給休暇 API

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/leaves/balance | 自分の有給残高 | 全員 |
| POST | /api/v1/leaves/requests | 有給申請 | 全員 |
| GET | /api/v1/leaves/requests | 自分の申請一覧 | 全員 |

#### GET /api/v1/leaves/balance

```yaml
Response: 200
  fiscalYear: number
  totalGrantedDays: number
  usedDays: number
  remainingDays: number
  carriedOverDays: number
  obligationDays: number  # 年5日義務の残り
  balances:
    - grantDate: string (date)
      expiryDate: string (date)
      grantedDays: number
      usedDays: number
      remainingDays: number
```

#### POST /api/v1/leaves/requests

```yaml
Request:
  leaveDate: string (date)
  leaveType: "FULL" | "HALF_AM" | "HALF_PM"
  reason: string | null

Response: 201
  id: number
  status: "PENDING"
  leaveDate: string (date)
  leaveType: string
```

---

### 申請・承認 API

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/approvals/pending | 未処理申請一覧 | 承認者 |
| GET | /api/v1/approvals/{id} | 申請詳細 | 承認者 |
| PUT | /api/v1/approvals/{id}/approve | 承認 | 承認者 |
| PUT | /api/v1/approvals/{id}/reject | 却下 | 承認者 |
| POST | /api/v1/time-corrections | 打刻修正申請 | 全員 |

#### POST /api/v1/time-corrections

```yaml
Request:
  targetDate: string (date)
  correctedClockIn: string (datetime) | null
  correctedClockOut: string (datetime) | null
  reason: string

Response: 201
  id: number
  status: "PENDING"
  targetDate: string (date)
```

#### PUT /api/v1/approvals/{id}/approve

```yaml
Response: 200
  id: number
  status: "APPROVED"
  approvedAt: string (datetime)
```

#### PUT /api/v1/approvals/{id}/reject

```yaml
Request:
  rejectionReason: string

Response: 200
  id: number
  status: "REJECTED"
  rejectionReason: string
```

---

### 管理者 API — 社員マスタ

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/admin/employees | 社員一覧 | 管理者 |
| GET | /api/v1/admin/employees/{id} | 社員詳細 | 管理者 |
| POST | /api/v1/admin/employees | 社員登録 | 管理者 |
| PUT | /api/v1/admin/employees/{id} | 社員編集 | 管理者 |
| PUT | /api/v1/admin/employees/{id}/deactivate | 社員無効化 | 管理者 |

#### POST /api/v1/admin/employees

```yaml
Request:
  employeeCode: string
  name: string
  email: string
  role: "EMPLOYEE" | "APPROVER" | "ADMIN"
  hireDate: string (date)
  departmentIds:
    - departmentId: number
      isPrimary: boolean

Response: 201
  id: number
  employeeCode: string
  name: string
  email: string
  role: string
  hireDate: string (date)
```

---

### 管理者 API — 部門マスタ

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/admin/departments | 部門ツリー取得 | 管理者 |
| POST | /api/v1/admin/departments | 部門登録 | 管理者 |
| PUT | /api/v1/admin/departments/{id} | 部門編集 | 管理者 |
| PUT | /api/v1/admin/departments/{id}/deactivate | 部門無効化 | 管理者 |

---

### 管理者 API — 勤務カレンダー

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/admin/calendars?fiscalYear={} | カレンダー取得 | 管理者 |
| POST | /api/v1/admin/calendars | 休日登録 | 管理者 |
| PUT | /api/v1/admin/calendars/{id} | 休日編集 | 管理者 |
| DELETE | /api/v1/admin/calendars/{id} | 休日削除 | 管理者 |

---

### 管理者 API — レポート・アラート

| Method | Path | 説明 | ロール |
|--------|------|------|--------|
| GET | /api/v1/admin/reports/overtime?year={}&month={} | 残業レポート | 管理者 |
| GET | /api/v1/admin/reports/leave-obligation?fiscalYear={} | 有給5日未取得一覧 | 管理者 |
| GET | /api/v1/admin/alerts | アラート一覧 | 管理者 |
| PUT | /api/v1/admin/alerts/{id}/acknowledge | アラート確認済み | 管理者 |

#### GET /api/v1/admin/reports/overtime

```yaml
Response: 200
  year: number
  month: number
  records:
    - employeeId: number
      employeeName: string
      departmentName: string
      overtimeMinutes: number
      yearlyOvertimeMinutes: number
      isOverMonthlyLimit: boolean
      isOverYearlyLimit: boolean
```

#### GET /api/v1/admin/alerts

```yaml
Response: 200
  content:
    - id: number
      employeeId: number
      employeeName: string
      type: string
      message: string
      createdAt: string (datetime)
      acknowledged: boolean
  page: number
  size: number
  totalElements: number
```

---

### 認証 API

| Method | Path | 説明 |
|--------|------|------|
| GET | /api/v1/auth/me | ログインユーザー情報取得 |
| POST | /api/v1/auth/logout | ログアウト |

#### GET /api/v1/auth/me

```yaml
Response: 200
  employeeId: number
  employeeCode: string
  name: string
  email: string
  role: string
  departments:
    - id: number
      name: string
      isPrimary: boolean
```

---

## エラーレスポンス形式

```json
{
  "type": "https://attendance.example.com/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "入力内容に誤りがあります",
  "errors": [
    {
      "field": "leaveDate",
      "message": "過去の日付は指定できません"
    }
  ]
}
```

## HTTP ステータスコード

| コード | 用途 |
|--------|------|
| 200 | 正常（取得・更新） |
| 201 | 正常（新規作成） |
| 400 | バリデーションエラー |
| 401 | 未認証 |
| 403 | 権限不足 |
| 404 | リソース未存在 |
| 409 | 競合（楽観ロック失敗） |
| 500 | サーバーエラー |
