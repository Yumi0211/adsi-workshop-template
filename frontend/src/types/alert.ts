export interface AlertItem {
  id: number;
  employeeId: number;
  employeeName: string;
  type: string;
  message: string;
  createdAt: string;
  acknowledged: boolean;
}
