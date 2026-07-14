import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TeamAttendancePage from "@/app/(main)/team/attendance/page";

const mockGet = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: { get: (...args: unknown[]) => mockGet(...args) },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({
    user: { employeeId: 1, name: "承認者", role: "APPROVER", departments: [{ id: 1, name: "開発部", isPrimary: true }] },
    isLoading: false,
    isAuthenticated: true,
  }),
}));

const MOCK_TEAM_ATTENDANCE = [
  { employeeId: 2, employeeName: "鈴木花子", workingDays: 10, totalWorkingMinutes: 4800, totalOvertimeMinutes: 300 },
  { employeeId: 3, employeeName: "山田太郎", workingDays: 9, totalWorkingMinutes: 4320, totalOvertimeMinutes: 120 },
];

describe("TeamAttendancePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_TEAM_ATTENDANCE);
  });

  it("部門メンバーの勤怠が表示される", async () => {
    render(<TeamAttendancePage />);
    await waitFor(() => {
      expect(screen.getByText("鈴木花子")).toBeInTheDocument();
      expect(screen.getByText("山田太郎")).toBeInTheDocument();
    });
  });

  it("勤務日数と残業時間が表示される", async () => {
    render(<TeamAttendancePage />);
    await waitFor(() => {
      expect(screen.getByText("10日")).toBeInTheDocument();
      expect(screen.getByText("9日")).toBeInTheDocument();
    });
  });

  it("API エラー時にエラーメッセージが表示される", async () => {
    mockGet.mockRejectedValue(new Error("API error"));
    render(<TeamAttendancePage />);
    await waitFor(() => {
      expect(screen.getByText("部門勤怠データの取得に失敗しました")).toBeInTheDocument();
    });
  });
});
