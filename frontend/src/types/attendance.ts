export interface AttendanceRecord {
  date: string;
  clockIn: string | null;
  clockOut: string | null;
  breakMinutes: number;
  workingMinutes: number;
  overtimeMinutes: number;
  nightMinutes: number;
  isHolidayWork: boolean;
  status: string;
}

export interface AttendanceSummary {
  totalWorkingMinutes: number;
  totalOvertimeMinutes: number;
  totalNightMinutes: number;
  totalHolidayWorkMinutes: number;
  workingDays: number;
}

export interface MonthlyAttendance {
  employeeId: number;
  year: number;
  month: number;
  records: AttendanceRecord[];
  summary: AttendanceSummary;
}
