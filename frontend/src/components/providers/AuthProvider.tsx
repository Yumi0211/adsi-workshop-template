"use client";

import { SessionProvider } from "next-auth/react";
import { type ReactNode } from "react";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "";

  return (
    <SessionProvider basePath={`${basePath}/api/auth`}>
      {children}
    </SessionProvider>
  );
}
