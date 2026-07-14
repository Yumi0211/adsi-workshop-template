import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ApprovalsPage from "@/app/(main)/approvals/page";

const mockGet = vi.fn();
const mockPut = vi.fn();
const mockReplace = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
    put: (...args: unknown[]) => mockPut(...args),
  },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({ user: { employeeId: 1, name: "承認者", role: "APPROVER", departments: [] }, isLoading: false, isAuthenticated: true }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mockReplace }),
  usePathname: () => "/approvals",
}));

const MOCK_PENDING = [
  { id: 1, type: "TIME_CORRECTION", employeeId: 2, employeeName: "鈴木花子", status: "PENDING", description: "7/10 退勤打刻忘れ。18:30退勤に修正希望", createdAt: "2026-07-11T08:00:00", approvedAt: null, rejectionReason: null },
  { id: 2, type: "LEAVE_REQUEST", employeeId: 3, employeeName: "山田太郎", status: "PENDING", description: "7/20 有給(全休) 私用のため", createdAt: "2026-07-12T09:00:00", approvedAt: null, rejectionReason: null },
];

describe("ApprovalsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_PENDING);
    mockPut.mockResolvedValue({ id: 1, status: "APPROVED", approvedAt: "2026-07-14T10:00:00" });
  });

  it("未処理申請一覧が表示される", async () => {
    render(<ApprovalsPage />);
    await waitFor(() => {
      expect(screen.getByText("鈴木花子")).toBeInTheDocument();
      expect(screen.getByText("山田太郎")).toBeInTheDocument();
    });
  });

  it("承認ボタンクリックで approve API が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<ApprovalsPage />);
    await waitFor(() => { expect(screen.getByText("鈴木花子")).toBeInTheDocument(); });
    const approveButtons = screen.getAllByRole("button", { name: "承認" });
    await user.click(approveButtons[0]);
    await waitFor(() => {
      expect(mockPut).toHaveBeenCalledWith("/api/v1/approvals/1/approve");
    });
  });

  it("却下ボタンクリックで reject API が呼ばれる", async () => {
    mockPut.mockResolvedValue({ id: 2, status: "REJECTED", rejectionReason: "理由" });
    const user = userEvent.setup();
    render(<ApprovalsPage />);
    await waitFor(() => { expect(screen.getByText("山田太郎")).toBeInTheDocument(); });
    const rejectButtons = screen.getAllByRole("button", { name: "却下" });
    await user.click(rejectButtons[0]);
    await waitFor(() => {
      expect(mockPut).toHaveBeenCalledWith("/api/v1/approvals/1/reject", expect.any(Object));
    });
  });

  it("API エラー時にエラーメッセージが表示される", async () => {
    mockGet.mockRejectedValue(new Error("API error"));
    render(<ApprovalsPage />);
    await waitFor(() => {
      expect(screen.getByText("承認依頼一覧の取得に失敗しました")).toBeInTheDocument();
    });
  });
});
