"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Modal } from "@/components/ui/Modal";
import type { ApprovalRequest } from "@/types/approval";

export default function ApprovalsPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [requests, setRequests] = useState<ApprovalRequest[]>([]);
  const [error, setError] = useState("");
  const [rejectTargetId, setRejectTargetId] = useState<number | null>(null);
  const [rejectionReason, setRejectionReason] = useState("");

  useEffect(() => {
    if (user && user.role === "EMPLOYEE") { router.replace("/"); }
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

  const openRejectModal = (id: number) => {
    setRejectTargetId(id);
    setRejectionReason("");
  };

  const handleReject = async () => {
    if (!rejectTargetId) return;
    if (!rejectionReason.trim()) { setError("却下理由を入力してください"); return; }
    setError("");
    try {
      await apiClient.put(`/api/v1/approvals/${rejectTargetId}/reject`, { rejectionReason });
      setRejectTargetId(null);
      setRejectionReason("");
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
                    <Button variant="danger" onClick={() => openRejectModal(req.id)} className="text-xs px-3 py-1">却下</Button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <Modal open={rejectTargetId !== null} onClose={() => setRejectTargetId(null)} title="却下理由">
        <div className="space-y-4">
          <div className="flex flex-col gap-1">
            <label htmlFor="reject-reason" className="text-sm font-medium text-gray-700">理由</label>
            <textarea
              id="reject-reason"
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              rows={3}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm"
            />
          </div>
          <div className="flex gap-2 justify-end">
            <Button variant="secondary" onClick={() => setRejectTargetId(null)}>キャンセル</Button>
            <Button variant="danger" onClick={handleReject}>却下する</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
