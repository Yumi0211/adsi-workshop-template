export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ApprovalRequest {
  id: number;
  type: string;
  employeeId: number;
  employeeName: string;
  status: ApprovalStatus;
  description: string;
  createdAt: string;
  approvedAt: string | null;
  rejectionReason: string | null;
}

export interface TimeCorrectionCreate {
  targetDate: string;
  correctedClockIn: string | null;
  correctedClockOut: string | null;
  reason: string;
}

export interface TimeCorrectionResponse {
  id: number;
  status: ApprovalStatus;
  targetDate: string;
}

export interface MyRequest {
  id: number;
  type: string;
  status: ApprovalStatus;
  description: string;
  createdAt: string;
  approvedAt: string | null;
  rejectionReason: string | null;
}
