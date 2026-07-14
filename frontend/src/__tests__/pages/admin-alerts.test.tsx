import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AdminAlertsPage from "@/app/(main)/admin/alerts/page";

const mockGet = vi.fn();
const mockPut = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
    put: (...args: unknown[]) => mockPut(...args),
  },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({ user: { employeeId: 1, name: "管理者", role: "ADMIN", departments: [] }, isLoading: false, isAuthenticated: true }),
}));

const MOCK_ALERTS = {
  content: [
    { id: 1, employeeId: 2, employeeName: "鈴木花子", type: "OVERTIME_MONTHLY", message: "月間残業が45hを超過", createdAt: "2026-07-10T08:00:00", acknowledged: false },
    { id: 2, employeeId: 3, employeeName: "山田太郎", type: "LEAVE_OBLIGATION", message: "有給5日取得義務 残り3日", createdAt: "2026-07-12T08:00:00", acknowledged: true },
  ],
  page: 0, size: 20, totalElements: 2,
};

describe("AdminAlertsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_ALERTS);
    mockPut.mockResolvedValue({});
  });

  it("アラート一覧が表示される", async () => {
    render(<AdminAlertsPage />);
    await waitFor(() => {
      expect(screen.getByText("鈴木花子")).toBeInTheDocument();
      expect(screen.getByText(/月間残業が45hを超過/)).toBeInTheDocument();
    });
  });

  it("確認済みボタンで acknowledge API が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<AdminAlertsPage />);
    await waitFor(() => { expect(screen.getByText("鈴木花子")).toBeInTheDocument(); });
    const ackButtons = screen.getAllByRole("button", { name: "確認済み" });
    await user.click(ackButtons[0]);
    await waitFor(() => {
      expect(mockPut).toHaveBeenCalledWith("/api/v1/admin/alerts/1/acknowledge");
    });
  });

  it("既に確認済みのアラートは確認済みバッジが表示される", async () => {
    render(<AdminAlertsPage />);
    await waitFor(() => {
      expect(screen.getByText("対応済み")).toBeInTheDocument();
    });
  });
});
