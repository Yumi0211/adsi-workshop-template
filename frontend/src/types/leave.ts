export type LeaveType = "FULL" | "HALF_AM" | "HALF_PM";
export type LeaveRequestStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface LeaveBalanceDetail {
  grantDate: string;
  expiryDate: string;
  grantedDays: number;
  usedDays: number;
  remainingDays: number;
}

export interface LeaveBalance {
  fiscalYear: number;
  totalGrantedDays: number;
  usedDays: number;
  remainingDays: number;
  carriedOverDays: number;
  obligationDays: number;
  balances: LeaveBalanceDetail[];
}

export interface LeaveRequestCreate {
  leaveDate: string;
  leaveType: LeaveType;
  reason: string | null;
}

export interface LeaveRequestResponse {
  id: number;
  status: LeaveRequestStatus;
  leaveDate: string;
  leaveType: LeaveType;
}
