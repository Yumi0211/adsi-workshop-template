# ドメインモデル設計

## パッケージ構成

```
com.example.attendance
├── employee/        # 社員・組織ドメイン
├── timerecord/      # 打刻・勤怠ドメイン
├── leave/           # 有給休暇ドメイン
├── approval/        # 申請・承認ドメイン
├── alert/           # アラートドメイン
└── admin/           # 管理者機能（マスタ管理）
```

---

## Entity

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK（自動採番） |
| employeeCode | String | 社員番号（一意） |
| name | String | 氏名 |
| email | String | メールアドレス（Entra ID 連携キー） |
| role | Role (enum) | EMPLOYEE / APPROVER / ADMIN |
| hireDate | LocalDate | 入社日 |
| active | boolean | 有効フラグ |
| version | Long | 楽観ロック |

### Department（部門）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| name | String | 部門名 |
| level | DepartmentLevel (enum) | HEADQUARTERS / DIVISION / SECTION |
| parentId | Long | 親部門ID（nullable、最上位は null） |
| active | boolean | 有効フラグ |
| version | Long | 楽観ロック |

### EmployeeDepartment（社員部門所属 — 兼務対応）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| departmentId | Long | FK → Department |
| isPrimary | boolean | 主所属フラグ |
| startDate | LocalDate | 所属開始日 |
| endDate | LocalDate | 所属終了日（nullable） |

### TimeRecord（打刻）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| recordDate | LocalDate | 打刻日 |
| type | TimeRecordType (enum) | CLOCK_IN / CLOCK_OUT / BREAK_START / BREAK_END |
| recordedAt | LocalDateTime | 打刻時刻 |
| source | RecordSource (enum) | IC_CARD / WEB |
| version | Long | 楽観ロック |

### DailyAttendance（日次勤怠 — 計算結果）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| attendanceDate | LocalDate | 勤務日 |
| clockIn | LocalDateTime | 出勤時刻 |
| clockOut | LocalDateTime | 退勤時刻（nullable） |
| breakMinutes | int | 休憩時間（分） |
| workingMinutes | int | 勤務時間（分） |
| overtimeMinutes | int | 残業時間（分） |
| nightMinutes | int | 深夜勤務時間（分） |
| isHolidayWork | boolean | 休日勤務フラグ |
| status | AttendanceStatus (enum) | PRESENT / ABSENT / HOLIDAY / PAID_LEAVE / HALF_LEAVE_AM / HALF_LEAVE_PM |
| version | Long | 楽観ロック |

### LeaveBalance（有給残高）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| fiscalYear | int | 年度 |
| grantedDays | BigDecimal | 付与日数 |
| usedDays | BigDecimal | 使用日数 |
| carriedOverDays | BigDecimal | 繰越日数 |
| grantDate | LocalDate | 付与日 |
| expiryDate | LocalDate | 失効日 |
| version | Long | 楽観ロック |

### ApprovalRequest（申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| applicantId | Long | FK → Employee（申請者） |
| approverId | Long | FK → Employee（承認者） |
| type | RequestType (enum) | TIME_CORRECTION / LEAVE / OVERTIME |
| status | RequestStatus (enum) | PENDING / APPROVED / REJECTED |
| requestDate | LocalDate | 申請日 |
| detail | String | 申請内容（JSON） |
| reason | String | 申請理由 |
| approvedAt | LocalDateTime | 承認日時（nullable） |
| rejectionReason | String | 却下理由（nullable） |
| version | Long | 楽観ロック |

### WorkCalendar（勤務カレンダー）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| calendarDate | LocalDate | 日付 |
| dayType | DayType (enum) | WORKDAY / HOLIDAY / COMPANY_HOLIDAY |
| description | String | 説明（祝日名等） |
| fiscalYear | int | 年度 |

### Alert（アラート）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee（対象社員） |
| type | AlertType (enum) | OVERTIME_MONTHLY / OVERTIME_YEARLY / LEAVE_OBLIGATION / INTERVAL_VIOLATION |
| message | String | アラートメッセージ |
| createdAt | LocalDateTime | 発生日時 |
| acknowledged | boolean | 確認済みフラグ |

---

## Value Object

### WorkDuration（勤務時間）

| フィールド | 型 | 説明 |
|-----------|---|------|
| minutes | int | 時間（分単位） |

- `toHours()`: 時間表示（7h30m 等）
- `isOvertime(standardMinutes)`: 残業判定

### TimeRange（時間範囲）

| フィールド | 型 | 説明 |
|-----------|---|------|
| start | LocalDateTime | 開始 |
| end | LocalDateTime | 終了 |

- `overlapMinutes(other)`: 重複時間計算（深夜帯判定に使用）
- `durationMinutes()`: 範囲の長さ（分）

---

## Enum

```java
enum Role { EMPLOYEE, APPROVER, ADMIN }
enum DepartmentLevel { HEADQUARTERS, DIVISION, SECTION }
enum TimeRecordType { CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END }
enum RecordSource { IC_CARD, WEB }
enum AttendanceStatus { PRESENT, ABSENT, HOLIDAY, PAID_LEAVE, HALF_LEAVE_AM, HALF_LEAVE_PM }
enum RequestType { TIME_CORRECTION, LEAVE, OVERTIME }
enum RequestStatus { PENDING, APPROVED, REJECTED }
enum DayType { WORKDAY, HOLIDAY, COMPANY_HOLIDAY }
enum AlertType { OVERTIME_MONTHLY, OVERTIME_YEARLY, LEAVE_OBLIGATION, INTERVAL_VIOLATION }
```

---

## Repository（interface）

| Repository | 主なメソッド |
|-----------|------------|
| EmployeeRepository | findByEmployeeCode, findByEmail, findByDepartmentId |
| DepartmentRepository | findByParentId, findByLevel |
| EmployeeDepartmentRepository | findByEmployeeId, findByDepartmentId |
| TimeRecordRepository | findByEmployeeIdAndRecordDate |
| DailyAttendanceRepository | findByEmployeeIdAndMonth, findByEmployeeIdAndDateRange |
| LeaveBalanceRepository | findByEmployeeIdAndFiscalYear |
| ApprovalRequestRepository | findByApproverId, findByApplicantId, findPendingByApproverId |
| WorkCalendarRepository | findByFiscalYear, findByDate |
| AlertRepository | findByEmployeeId, findUnacknowledged |

---

## Service

| Service | 責務 |
|---------|------|
| TimeRecordService | 打刻の記録・バリデーション |
| AttendanceCalculationService | 日次勤怠の計算（勤務時間・残業・深夜） |
| LeaveService | 有給付与・残高管理・取得日数計算 |
| ApprovalService | 申請の作成・承認・却下 |
| OvertimeAlertService | 36協定の閾値チェック・アラート生成 |
| IntervalCheckService | 勤務間インターバルのチェック |
| EmployeeService | 社員 CRUD・ロール管理 |
| DepartmentService | 部門 CRUD・階層管理 |
| WorkCalendarService | カレンダー設定・休日判定 |
| MonthlyReportService | 月次集計・レポート生成 |

---

## ドメインルール（主要なもの）

| ルール | 説明 |
|--------|------|
| 打刻順序 | 出勤 → (休憩開始 → 休憩終了)* → 退勤 の順序を守る |
| 二重打刻防止 | 同日に出勤が2回記録されない |
| コアタイム | 10:00〜15:00 は勤務必須（アラート対象） |
| 残業閾値 | 月45h超過で承認必要、年360h超過でアラート |
| 深夜帯 | 22:00〜05:00 の勤務時間を別途計算 |
| 勤務間インターバル | 前日退勤〜当日出勤 < 11h でアラート |
| 有給付与 | 入社日起算、勤続年数に応じた法定日数 |
| 有給時効 | 付与から2年で失効 |
| 年5日義務 | 年度内に5日以上取得しないとアラート |

---

## 関連図（概要）

```
Employee ──┬── 1:N ──→ TimeRecord
           ├── 1:N ──→ DailyAttendance
           ├── 1:N ──→ LeaveBalance
           ├── 1:N ──→ ApprovalRequest (applicant)
           ├── 1:N ──→ ApprovalRequest (approver)
           ├── 1:N ──→ Alert
           └── N:M ──→ Department (via EmployeeDepartment)

Department ──→ self (parent)

WorkCalendar（独立、全社共通）
```
