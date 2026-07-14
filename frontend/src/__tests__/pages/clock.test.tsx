import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ClockPage from "@/app/(main)/clock/page";

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
      employeeCode: "EMP001",
      name: "田中太郎",
      email: "tanaka@example.com",
      role: "EMPLOYEE",
      departments: [{ id: 1, name: "開発部", isPrimary: true }],
    },
    isLoading: false,
    isAuthenticated: true,
    login: vi.fn(),
    logout: vi.fn(),
  }),
}));

describe("ClockPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue([]);
  });

  it("出勤打刻ボタンが表示される", async () => {
    render(<ClockPage />);
    await waitFor(() => {
      expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument();
    });
  });

  it("退勤打刻ボタンが表示される", async () => {
    render(<ClockPage />);
    await waitFor(() => {
      expect(screen.getByRole("button", { name: "退勤" })).toBeInTheDocument();
    });
  });

  it("休憩開始ボタンが表示される", async () => {
    render(<ClockPage />);
    await waitFor(() => {
      expect(screen.getByRole("button", { name: "休憩開始" })).toBeInTheDocument();
    });
  });

  it("休憩終了ボタンが表示される", async () => {
    render(<ClockPage />);
    await waitFor(() => {
      expect(screen.getByRole("button", { name: "休憩終了" })).toBeInTheDocument();
    });
  });

  it("出勤ボタンクリックで API が呼ばれる", async () => {
    mockPost.mockResolvedValue({ id: 1, type: "CLOCK_IN", recordedAt: "2026-07-14T09:00:00" });
    const user = userEvent.setup();
    render(<ClockPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument();
    });
    await user.click(screen.getByRole("button", { name: "出勤" }));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/api/v1/time-records", {
        type: "CLOCK_IN",
        source: "WEB",
      });
    });
  });

  it("今日の打刻履歴が表示される", async () => {
    mockGet.mockResolvedValue([
      { id: 1, type: "CLOCK_IN", recordedAt: "2026-07-14T09:02:00", source: "WEB" },
      { id: 2, type: "BREAK_START", recordedAt: "2026-07-14T12:00:00", source: "WEB" },
    ]);
    render(<ClockPage />);

    await waitFor(() => {
      expect(screen.getByText("09:02")).toBeInTheDocument();
      expect(screen.getByText("12:00")).toBeInTheDocument();
    });
  });
});
