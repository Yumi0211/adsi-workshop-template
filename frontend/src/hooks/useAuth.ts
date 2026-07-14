"use client";

import { useSession, signIn, signOut } from "next-auth/react";
import type { User } from "@/types";

export function useAuth() {
  const { data: session, status } = useSession();

  const user: User | null = session?.user as User | null;
  const isLoading = status === "loading";
  const isAuthenticated = status === "authenticated";

  const login = () => signIn("azure-ad");
  const logout = () => signOut({ callbackUrl: "/login" });

  return { user, isLoading, isAuthenticated, login, logout };
}
