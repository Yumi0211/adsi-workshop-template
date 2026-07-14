export type ClockType = "CLOCK_IN" | "CLOCK_OUT" | "BREAK_START" | "BREAK_END";
export type ClockSource = "IC_CARD" | "WEB";

export interface TimeRecord {
  id: number;
  employeeId: number;
  recordDate: string;
  type: ClockType;
  recordedAt: string;
  source: ClockSource;
}

export interface ClockRequest {
  type: ClockType;
  source: ClockSource;
}
