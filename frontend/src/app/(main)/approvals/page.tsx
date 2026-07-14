"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { ApprovalRequest } from "@/types/approval";

export default function ApprovalsPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [requests, setRequests] = useState<ApprovalRequest[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (user && user.role === "EMPLOYEE") {
      router.replace("/");
    }
  }, [user, router]);

  const fetchPending = useCallback(async () => {
    try {
      const data = await apiClient.get<ApprovalRequest[]>("/api/v1/approvals/pending");
      setRequests(data);
    } catch {
      setError("承認依頼一覧の取得に失敗しました");
    }
  }, []);

  useEffect(() => {
    if (user && user.role !== "EMPLOYEE") { fetchPending(); }
  }, [user, fetchPending]);

  const handleApprove = async (id: number) => {
    setError("");
    try {
      await apiClient.put(`/api/v1/approvals/${id}/approve`);
      await fetchPending();
    } catch {
      setError("承認に失敗しました");
    }
  };

  const handleReject = async (id: number) => {
    setError("");
    try {
      await apiClient.put(`/api/v1/approvals/${id}/reject`, { rejectionReason: "" });
      await fetchPending();
    } catch {
      setError("却下に失敗しました");
    }
  };

  if (user && user.role === "EMPLOYEE") return null;

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">承認依頼一覧</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        {requests.length === 0 ? (
          <p className="text-sm text-gray-500">未処理の承認依頼はありません</p>
        ) : (
          <ul className="divide-y divide-gray-200">
            {requests.map((req) => (
              <li key={req.id} className="py-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm font-medium text-gray-900">{req.employeeName}</p>
                    <p className="text-xs text-gray-500">{req.type}</p>
                    <p className="text-sm text-gray-700 mt-1">{req.description}</p>
                    <p className="text-xs text-gray-400 mt-1">{new Date(req.createdAt).toLocaleDateString("ja-JP")}</p>
                  </div>
                  <div className="flex gap-2 flex-shrink-0">
                    <Button onClick={() => handleApprove(req.id)} className="text-xs px-3 py-1">承認</Button>
                    <Button variant="danger" onClick={() => handleReject(req.id)} className="text-xs px-3 py-1">却下</Button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
