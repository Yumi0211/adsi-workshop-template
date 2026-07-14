import { renderHook } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";

describe("useAuth", () => {
  const originalEnv = process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS;

  beforeEach(() => {
    vi.resetModules();
  });

  afterEach(() => {
    if (originalEnv === undefined) {
      delete process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS;
    } else {
      process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS = originalEnv;
    }
  });

  describe("DEV_AUTH_BYPASS=true のとき", () => {
    beforeEach(() => {
      process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS = "true";
    });

    it("固定の開発ユーザー（ADMIN）を返す", async () => {
      vi.doMock("next-auth/react", () => ({
        useSession: () => ({ data: null, status: "unauthenticated" }),
        signIn: vi.fn(),
        signOut: vi.fn(),
        SessionProvider: ({ children }: { children: React.ReactNode }) => children,
      }));

      const { useAuth } = await import("@/hooks/useAuth");
      const { result } = renderHook(() => useAuth());

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isLoading).toBe(false);
      expect(result.current.user).toEqual({
        employeeId: 9999,
        employeeCode: "DEV999",
        name: "開発ユーザー",
        email: "dev@example.com",
        role: "ADMIN",
        departments: [{ id: 1, name: "開発部", isPrimary: true }],
      });
    });

    it("login/logout は no-op", async () => {
      vi.doMock("next-auth/react", () => ({
        useSession: () => ({ data: null, status: "unauthenticated" }),
        signIn: vi.fn(),
        signOut: vi.fn(),
        SessionProvider: ({ children }: { children: React.ReactNode }) => children,
      }));

      const { useAuth } = await import("@/hooks/useAuth");
      const { result } = renderHook(() => useAuth());

      expect(() => result.current.login()).not.toThrow();
      expect(() => result.current.logout()).not.toThrow();
    });
  });

  describe("DEV_AUTH_BYPASS が未設定のとき", () => {
    beforeEach(() => {
      delete process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS;
    });

    it("未認証の場合、isAuthenticated=false を返す", async () => {
      vi.doMock("next-auth/react", () => ({
        useSession: () => ({ data: null, status: "unauthenticated" }),
        signIn: vi.fn(),
        signOut: vi.fn(),
        SessionProvider: ({ children }: { children: React.ReactNode }) => children,
      }));

      const { useAuth } = await import("@/hooks/useAuth");
      const { result } = renderHook(() => useAuth());

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isLoading).toBe(false);
      expect(result.current.user).toBeFalsy();
    });

    it("認証済みの場合、セッションユーザーを返す", async () => {
      const mockUser = {
        employeeId: 1,
        employeeCode: "EMP001",
        name: "テストユーザー",
        email: "test@example.com",
        role: "EMPLOYEE",
        departments: [],
      };

      vi.doMock("next-auth/react", () => ({
        useSession: () => ({
          data: { user: mockUser },
          status: "authenticated",
        }),
        signIn: vi.fn(),
        signOut: vi.fn(),
        SessionProvider: ({ children }: { children: React.ReactNode }) => children,
      }));

      const { useAuth } = await import("@/hooks/useAuth");
      const { result } = renderHook(() => useAuth());

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUser);
    });
  });
});
