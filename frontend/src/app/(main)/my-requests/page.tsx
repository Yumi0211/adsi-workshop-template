"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Card } from "@/components/ui/Card";
import type { MyRequest, ApprovalStatus } from "@/types/approval";

const STATUS_LABELS: Record<ApprovalStatus, { text: string; className: string }> = {
  PENDING: { text: "承認待ち", className: "bg-yellow-100 text-yellow-800" },
  APPROVED: { text: "承認済み", className: "bg-green-100 text-green-800" },
  REJECTED: { text: "却下", className: "bg-red-100 text-red-800" },
};

export default function MyRequestsPage() {
  const [requests, setRequests] = useState<MyRequest[]>([]);
  const [error, setError] = useState("");

  const fetchRequests = useCallback(async () => {
    try {
      const data = await apiClient.get<MyRequest[]>("/api/v1/my-requests");
      setRequests(data);
    } catch {
      setError("申請一覧の取得に失敗しました");
    }
  }, []);

  useEffect(() => { fetchRequests(); }, [fetchRequests]);

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">申請一覧</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        {requests.length === 0 ? (
          <p className="text-sm text-gray-500">申請はありません</p>
        ) : (
          <ul className="divide-y divide-gray-200">
            {requests.map((req) => {
              const statusInfo = STATUS_LABELS[req.status];
              return (
                <li key={req.id} className="py-3 flex items-start justify-between gap-4">
                  <div>
                    <p className="text-sm text-gray-900">{req.description}</p>
                    <p className="text-xs text-gray-400 mt-1">{new Date(req.createdAt).toLocaleDateString("ja-JP")}</p>
                    {req.rejectionReason && (
                      <p className="text-xs text-red-600 mt-1">却下理由: {req.rejectionReason}</p>
                    )}
                  </div>
                  <span className={`text-xs font-medium px-2 py-1 rounded-full ${statusInfo.className}`}>
                    {statusInfo.text}
                  </span>
                </li>
              );
            })}
          </ul>
        )}
      </Card>
    </div>
  );
}
