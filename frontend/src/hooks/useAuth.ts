"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import type { User } from "@/types";

const IS_DEV = process.env.NODE_ENV === "development";

const DEV_USER: User = {
  name: "開発ユーザー",
  email: "dev@example.com",
  role: "ADMIN",
};

export function useAuth() {
  const { data: session, status } = useSession();

  if (IS_DEV) {
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
