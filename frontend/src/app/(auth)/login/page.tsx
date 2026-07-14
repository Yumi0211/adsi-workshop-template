"use client";

import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";

export default function LoginPage() {
  const { login } = useAuth();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-sm">
        <h1 className="text-2xl font-bold text-center text-gray-900 mb-6">
          勤怠管理システム
        </h1>
        <p className="text-sm text-gray-600 text-center mb-6">
          Microsoft アカウントでログインしてください
        </p>
        <Button onClick={login} className="w-full">
          ログイン
        </Button>
      </div>
    </div>
  );
}
