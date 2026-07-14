import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AttendancePage from "@/app/(main)/attendance/page";

const mockGet = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
  },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({
    user: {
      employeeId: 1,
      name: "田中太郎",
      role: "EMPLOYEE",
      departments: [],
    },
    isLoading: false,
    isAuthenticated: true,
  }),
}));

const MOCK_MONTHLY_ATTENDANCE = {
  employeeId: 1,
  year: 2026,
  month: 7,
  records: [
    {
      date: "2026-07-01",
      clockIn: "2026-07-01T09:00:00",
      clockOut: "2026-07-01T18:00:00",
      breakMinutes: 60,
      workingMinutes: 480,
      overtimeMinutes: 30,
      nightMinutes: 0,
      isHolidayWork: false,
      status: "出勤",
    },
    {
      date: "2026-07-02",
      clockIn: null,
      clockOut: null,
      breakMinutes: 0,
      workingMinutes: 0,
      overtimeMinutes: 0,
      nightMinutes: 0,
      isHolidayWork: false,
      status: "有給(全休)",
    },
  ],
  summary: {
    totalWorkingMinutes: 480,
    totalOvertimeMinutes: 30,
    totalNightMinutes: 0,
    totalHolidayWorkMinutes: 0,
    workingDays: 1,
  },
};

describe("AttendancePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_MONTHLY_ATTENDANCE);
  });

  it("月次勤怠データがテーブルに表示される", async () => {
    render(<AttendancePage />);
    await waitFor(() => {
      expect(screen.getByText("07/01")).toBeInTheDocument();
      expect(screen.getByText("09:00")).toBeInTheDocument();
      expect(screen.getByText("有給(全休)")).toBeInTheDocument();
    });
  });

  it("月間サマリーが表示される", async () => {
    render(<AttendancePage />);
    await waitFor(() => {
      expect(screen.getByText(/勤務日数/)).toBeInTheDocument();
      expect(screen.getByText(/1日/)).toBeInTheDocument();
    });
  });

  it("前月ボタンで月を移動できる", async () => {
    const user = userEvent.setup();
    render(<AttendancePage />);

    await waitFor(() => {
      expect(screen.getByText(/2026年7月/)).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "前月" }));

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        expect.stringContaining("year=2026&month=6")
      );
    });
  });

  it("次月ボタンで月を移動できる", async () => {
    const user = userEvent.setup();
    render(<AttendancePage />);

    await waitFor(() => {
      expect(screen.getByText(/2026年7月/)).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "次月" }));

    await waitFor(() => {
      expect(mockGet).toHaveBeenCalledWith(
        expect.stringContaining("year=2026&month=8")
      );
    });
  });
});
