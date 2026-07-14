import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AdminDepartmentsPage from "@/app/(main)/admin/departments/page";

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
      name: "管理者",
      role: "ADMIN",
      departments: [],
    },
    isLoading: false,
    isAuthenticated: true,
  }),
}));

const MOCK_DEPARTMENTS = [
  {
    id: 1,
    name: "本社",
    parentId: null,
    active: true,
    children: [
      { id: 2, name: "開発部", parentId: 1, active: true, children: [] },
      { id: 3, name: "人事部", parentId: 1, active: true, children: [] },
    ],
  },
];

describe("AdminDepartmentsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGet.mockResolvedValue(MOCK_DEPARTMENTS);
    mockPost.mockResolvedValue({ id: 4, name: "営業部", parentId: 1, active: true });
  });

  it("部門一覧が表示される", async () => {
    render(<AdminDepartmentsPage />);
    await waitFor(() => {
      expect(screen.getAllByText("本社").length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText("開発部").length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText("人事部").length).toBeGreaterThanOrEqual(1);
    });
  });

  it("新規登録フォームで部門を作成できる", async () => {
    const user = userEvent.setup();
    render(<AdminDepartmentsPage />);

    await waitFor(() => {
      expect(screen.getByLabelText("部門名")).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText("部門名"), "営業部");
    await user.click(screen.getByRole("button", { name: "追加" }));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/api/v1/admin/departments", {
        name: "営業部",
        parentId: null,
      });
    });
  });
});
