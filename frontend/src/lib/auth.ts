import type { NextAuthOptions } from "next-auth";
import AzureADProvider from "next-auth/providers/azure-ad";
import type { Role } from "@/types";

const VALID_ROLES: Role[] = ["EMPLOYEE", "APPROVER", "ADMIN"];

function isValidRole(value: unknown): value is Role {
  return typeof value === "string" && VALID_ROLES.includes(value as Role);
}

function getRequiredEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    throw new Error(`${key} is required but not set`);
  }
  return value;
}

export const authOptions: NextAuthOptions = {
  providers: [
    ...(process.env.AZURE_AD_CLIENT_ID
      ? [
          AzureADProvider({
            clientId: getRequiredEnv("AZURE_AD_CLIENT_ID"),
            clientSecret: getRequiredEnv("AZURE_AD_CLIENT_SECRET"),
            tenantId: process.env.AZURE_AD_TENANT_ID,
          }),
        ]
      : []),
  ],
  pages: {
    signIn: "/login",
  },
  callbacks: {
    async jwt({ token, account, profile }) {
      if (account && profile) {
        const profileRole = (profile as Record<string, unknown>).role;
        token.role = isValidRole(profileRole) ? profileRole : "EMPLOYEE";
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        (session.user as Record<string, unknown>).role = token.role;
      }
      return session;
    },
  },
};
