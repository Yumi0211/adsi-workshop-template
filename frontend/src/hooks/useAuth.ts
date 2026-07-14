"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import type { User } from "@/types";

const IS_DEV_AUTH_BYPASS =
  process.env.NEXT_PUBLIC_DEV_AUTH_BYPASS === "true";

const DEV_USER: User = {
  employeeId: 9999,
  employeeCode: "DEV999",
  name: "開発ユーザー",
  email: "dev@example.com",
  role: "ADMIN",
  departments: [{ id: 1, name: "開発部", isPrimary: true }],
};

export function useAuth() {
  const { data: session, status } = useSession();

  if (IS_DEV_AUTH_BYPASS) {
    return {
      user: DEV_USER,
      isLoading: false,
      isAuthenticated: true,
      login: () => {},
      logout: () => {},
    };
  }

  const user: User | null = session?.user as User | null;
  const isLoading = status === "loading";
  const isAuthenticated = status === "authenticated";

  const login = () => signIn("azure-ad");
  const logout = () => signOut({ callbackUrl: "/login" });

  return { user, isLoading, isAuthenticated, login, logout };
}
