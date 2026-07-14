import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import CorrectionsNewPage from "@/app/(main)/corrections/new/page";

const mockPost = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: { post: (...args: unknown[]) => mockPost(...args) },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({ user: { employeeId: 1, name: "田中太郎", role: "EMPLOYEE", departments: [] }, isLoading: false, isAuthenticated: true }),
}));

describe("CorrectionsNewPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockPost.mockResolvedValue({ id: 1, status: "PENDING", targetDate: "2026-07-10" });
  });

  it("打刻修正申請フォームが表示される", () => {
    render(<CorrectionsNewPage />);
    expect(screen.getByLabelText("対象日")).toBeInTheDocument();
    expect(screen.getByLabelText("理由")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "申請" })).toBeInTheDocument();
  });

  it("フォーム入力後に申請 API が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<CorrectionsNewPage />);

    await user.type(screen.getByLabelText("対象日"), "2026-07-10");
    await user.type(screen.getByLabelText("修正出勤時刻"), "09:00");
    await user.type(screen.getByLabelText("理由"), "打刻忘れ");
    await user.click(screen.getByRole("button", { name: "申請" }));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/api/v1/time-corrections", expect.objectContaining({
        targetDate: "2026-07-10",
        reason: "打刻忘れ",
      }));
    });
  });

  it("対象日未入力で申請するとバリデーションエラーが表示される", async () => {
    const user = userEvent.setup();
    render(<CorrectionsNewPage />);
    await user.click(screen.getByRole("button", { name: "申請" }));
    await waitFor(() => {
      expect(screen.getByText("対象日を入力してください")).toBeInTheDocument();
    });
    expect(mockPost).not.toHaveBeenCalled();
  });

  it("理由未入力で申請するとバリデーションエラーが表示される", async () => {
    const user = userEvent.setup();
    render(<CorrectionsNewPage />);
    await user.type(screen.getByLabelText("対象日"), "2026-07-10");
    await user.click(screen.getByRole("button", { name: "申請" }));
    await waitFor(() => {
      expect(screen.getByText("理由を入力してください")).toBeInTheDocument();
    });
    expect(mockPost).not.toHaveBeenCalled();
  });
});
