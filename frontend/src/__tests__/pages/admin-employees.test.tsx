import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AdminEmployeesPage from "@/app/(main)/admin/employees/page";

const mockGet = vi.fn();
const mockPost = vi.fn();
const mockPut = vi.fn();

vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: (...args: unknown[]) => mockPut(...args),
  },
  withBasePath: (path: string) => path,
}));

vi.mock("@/hooks/useAuth", () => ({
  useAuth: () => ({
    user: {
      employeeId: 1,
      name: "管理者",
      role: "ADMIN",
      departments: [],
    },
    isLoading: false,
    isAuthenticated: true,
  }),
}));

const MOCK_EMPLOYEES = {
  content: [
    {
      id: 1,
      employeeCode: "EMP001",
      name: "田中太郎",
      email: "tanaka@example.com",
      role: "EMPLOYEE",
      hireDate: "2020-04-01",
      active: true,
      departments: [{ departmentId: 1, departmentName: "開発部", isPrimary: true }],
    },
    {
      id: 2,
      employeeCode: "EMP002",
      name: "鈴木花子",
      email: "suzuki@example.com",
      role: "APPROVER",
      hireDate: "2019-04-01",
      active: true,
      departments: [{ departmentId: 2, departmentName: "人事部", isPrimary: true }],
    },
  ],
  page: 0,
  size: 20,
  totalElements: 2,
};

describe("AdminEmployeesPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_EMPLOYEES);
  });

  it("社員一覧が表示される", async () => {
    render(<AdminEmployeesPage />);
    await waitFor(() => {
      expect(screen.getByText("田中太郎")).toBeInTheDocument();
      expect(screen.getByText("鈴木花子")).toBeInTheDocument();
    });
  });

  it("社員コードが表示される", async () => {
    render(<AdminEmployeesPage />);
    await waitFor(() => {
      expect(screen.getByText("EMP001")).toBeInTheDocument();
      expect(screen.getByText("EMP002")).toBeInTheDocument();
    });
  });

  it("新規登録ボタンでモーダルが開く", async () => {
    const user = userEvent.setup();
    render(<AdminEmployeesPage />);

    await waitFor(() => {
      expect(screen.getByRole("button", { name: "新規登録" })).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: "新規登録" }));

    await waitFor(() => {
      expect(screen.getByText("社員登録")).toBeInTheDocument();
    });
  });

  it("社員を無効化できる", async () => {
    mockPut.mockResolvedValue({});
    const user = userEvent.setup();
    render(<AdminEmployeesPage />);

    await waitFor(() => {
      expect(screen.getAllByRole("button", { name: "無効化" })).toHaveLength(2);
    });

    await user.click(screen.getAllByRole("button", { name: "無効化" })[0]);

    await waitFor(() => {
      expect(mockPut).toHaveBeenCalledWith("/api/v1/admin/employees/1/deactivate");
    });
  });
});
