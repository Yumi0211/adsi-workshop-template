import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import LeavesPage from "@/app/(main)/leaves/page";

const mockGet = vi.fn();
const mockPost = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
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

const MOCK_BALANCE = {
  fiscalYear: 2026,
  totalGrantedDays: 20,
  usedDays: 5,
  remainingDays: 15,
  carriedOverDays: 2,
  obligationDays: 3,
  balances: [
    {
      grantDate: "2026-04-01",
      expiryDate: "2028-03-31",
      grantedDays: 20,
      usedDays: 5,
      remainingDays: 15,
    },
  ],
};

describe("LeavesPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_BALANCE);
    mockPost.mockResolvedValue({ id: 1, status: "PENDING", leaveDate: "2026-08-01", leaveType: "FULL" });
  });

  it("有給残高が表示される", async () => {
    render(<LeavesPage />);
    await waitFor(() => {
      expect(screen.getByText("残日数")).toBeInTheDocument();
      expect(screen.getAllByText("15").length).toBeGreaterThanOrEqual(1);
    });
  });

  it("年5日義務の残りが表示される", async () => {
    render(<LeavesPage />);
    await waitFor(() => {
      expect(screen.getByText(/5日義務/)).toBeInTheDocument();
    });
  });

  it("申請フォームから有給申請ができる", async () => {
    const user = userEvent.setup();
    render(<LeavesPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("取得日")).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText("取得日"), "2026-08-01");
    await user.click(screen.getByRole("button", { name: "申請" }));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/api/v1/leaves/requests", expect.objectContaining({
        leaveDate: "2026-08-01",
        leaveType: "FULL",
      }));
    });
  });

  it("バリデーションエラー: 日付未入力で申請ボタンを押すとエラー表示", async () => {
    const user = userEvent.setup();
    render(<LeavesPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "申請" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "申請" }));

    await waitFor(() => {
      expect(screen.getByText("取得日を入力してください")).toBeInTheDocument();
    });
  });
});
