import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import MyRequestsPage from "@/app/(main)/my-requests/page";

const mockGet = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: { get: (...args: unknown[]) => mockGet(...args) },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({ user: { employeeId: 1, name: "田中太郎", role: "EMPLOYEE", departments: [] }, isLoading: false, isAuthenticated: true }),
}));

const MOCK_REQUESTS = [
  { id: 1, type: "TIME_CORRECTION", status: "PENDING", description: "7/10 打刻修正", createdAt: "2026-07-11T10:00:00", approvedAt: null, rejectionReason: null },
  { id: 2, type: "LEAVE_REQUEST", status: "APPROVED", description: "7/20 有給(全休)", createdAt: "2026-07-05T09:00:00", approvedAt: "2026-07-06T10:00:00", rejectionReason: null },
  { id: 3, type: "LEAVE_REQUEST", status: "REJECTED", description: "7/25 有給(全休)", createdAt: "2026-07-08T09:00:00", approvedAt: null, rejectionReason: "繁忙期のため" },
];

describe("MyRequestsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_REQUESTS);
  });

  it("自分の申請一覧が表示される", async () => {
    render(<MyRequestsPage />);
    await waitFor(() => {
      expect(screen.getByText("7/10 打刻修正")).toBeInTheDocument();
      expect(screen.getByText("7/20 有給(全休)")).toBeInTheDocument();
    });
  });

  it("ステータスバッジが表示される", async () => {
    render(<MyRequestsPage />);
    await waitFor(() => {
      expect(screen.getByText("承認待ち")).toBeInTheDocument();
      expect(screen.getByText("承認済み")).toBeInTheDocument();
      expect(screen.getByText("却下")).toBeInTheDocument();
    });
  });

  it("API エラー時にエラーメッセージが表示される", async () => {
    mockGet.mockRejectedValue(new Error("API error"));
    render(<MyRequestsPage />);
    await waitFor(() => {
      expect(screen.getByText("申請一覧の取得に失敗しました")).toBeInTheDocument();
    });
  });
});
