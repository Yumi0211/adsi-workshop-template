import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import DashboardPage from "@/app/(main)/page";

const mockGet = vi.fn();
const mockPost = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
  },
  withBasePath: (path: string) => path,
}));

const mockUseAuth = vi.fn();
vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => mockUseAuth(),
}));

const MOCK_TIME_RECORDS = [
  { id: 1, type: "CLOCK_IN", recordedAt: "2026-07-14T09:02:00", source: "WEB" },
];

const MOCK_MONTHLY = {
  employeeId: 1, year: 2026, month: 7,
  records: [],
  summary: { totalWorkingMinutes: 4800, totalOvertimeMinutes: 750, totalNightMinutes: 0, totalHolidayWorkMinutes: 0, workingDays: 10 },
};

const MOCK_ALERTS = { content: [{ id: 1, employeeId: 1, employeeName: "田中太郎", type: "OVERTIME_MONTHLY", message: "残業時間が30hを超えました", createdAt: "2026-07-14T10:00:00", acknowledged: false }], page: 0, size: 20, totalElements: 1 };

describe("DashboardPage (EMPLOYEE)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { employeeId: 1, name: "田中太郎", role: "EMPLOYEE", departments: [] },
      isLoading: false, isAuthenticated: true, login: vi.fn(), logout: vi.fn(),
    });
    mockGet.mockImplementation((path: string) => {
      if (path.includes("/time-records")) return Promise.resolve(MOCK_TIME_RECORDS);
      if (path.includes("/attendances/monthly")) return Promise.resolve(MOCK_MONTHLY);
      if (path.includes("/alerts")) return Promise.resolve(MOCK_ALERTS);
      return Promise.resolve(null);
    });
  });

  it("打刻ボタンが表示される", async () => {
    render(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "退勤" })).toBeInTheDocument();
    });
  });

  it("今日の打刻状態が表示される", async () => {
    render(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getAllByText(/09:02/).length).toBeGreaterThanOrEqual(1);
    });
  });

  it("今月のサマリーが表示される", async () => {
    render(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText(/10日/)).toBeInTheDocument();
    });
  });

  it("出勤ボタンクリックで打刻 API が呼ばれる", async () => {
    mockPost.mockResolvedValue({ id: 2, type: "CLOCK_IN", recordedAt: "2026-07-14T09:00:00" });
    const user = userEvent.setup();
    render(<DashboardPage />);
    await waitFor(() => { expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument(); });
    await user.click(screen.getByRole("button", { name: "出勤" }));
    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/api/v1/time-records", { type: "CLOCK_IN", source: "WEB" });
    });
  });

  it("EMPLOYEE ロールではアラートが表示されない", async () => {
    render(<DashboardPage />);
    await waitFor(() => { expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument(); });
    expect(screen.queryByText("通知")).not.toBeInTheDocument();
  });
});

describe("DashboardPage (ADMIN)", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { employeeId: 1, name: "管理者", role: "ADMIN", departments: [] },
      isLoading: false, isAuthenticated: true, login: vi.fn(), logout: vi.fn(),
    });
    mockGet.mockImplementation((path: string) => {
      if (path.includes("/time-records")) return Promise.resolve(MOCK_TIME_RECORDS);
      if (path.includes("/attendances/monthly")) return Promise.resolve(MOCK_MONTHLY);
      if (path.includes("/alerts")) return Promise.resolve(MOCK_ALERTS);
      return Promise.resolve(null);
    });
  });

  it("ADMIN ロールではアラート通知が表示される", async () => {
    render(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("通知")).toBeInTheDocument();
      expect(screen.getByText("残業時間が30hを超えました")).toBeInTheDocument();
    });
  });
});
