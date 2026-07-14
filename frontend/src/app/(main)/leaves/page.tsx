"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { LeaveBalance, LeaveType } from "@/types/leave";

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  FULL: "全休",
  HALF_AM: "午前半休",
  HALF_PM: "午後半休",
};

export default function LeavesPage() {
  const [balance, setBalance] = useState<LeaveBalance | null>(null);
  const [leaveDate, setLeaveDate] = useState("");
  const [leaveType, setLeaveType] = useState<LeaveType>("FULL");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const fetchBalance = useCallback(async () => {
    const data = await apiClient.get<LeaveBalance>("/api/v1/leaves/balance");
    setBalance(data);
  }, []);

  useEffect(() => {
    fetchBalance();
  }, [fetchBalance]);

  const handleSubmit = async () => {
    setError("");
    if (!leaveDate) {
      setError("取得日を入力してください");
      return;
    }

    setSubmitting(true);
    try {
      await apiClient.post("/api/v1/leaves/requests", {
        leaveDate,
        leaveType,
        reason: reason || null,
      });
      setLeaveDate("");
      setReason("");
      await fetchBalance();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">有給休暇</h2>

      {balance && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card>
            <p className="text-sm text-gray-500">残日数</p>
            <p className="text-3xl font-bold text-primary-600">{balance.remainingDays}</p>
            <p className="text-xs text-gray-400">/ {balance.totalGrantedDays}日付与</p>
          </Card>
          <Card>
            <p className="text-sm text-gray-500">取得済み</p>
            <p className="text-3xl font-bold text-gray-900">{balance.usedDays}</p>
            <p className="text-xs text-gray-400">日</p>
          </Card>
          <Card>
            <p className="text-sm text-gray-500">5日義務 残り</p>
            <p className="text-3xl font-bold text-yellow-600">{balance.obligationDays}</p>
            <p className="text-xs text-gray-400">日</p>
          </Card>
        </div>
      )}

      <Card title="有給申請">
        <div className="space-y-4">
          <div className="flex flex-col gap-1">
            <label htmlFor="leave-date" className="text-sm font-medium text-gray-700">
              取得日
            </label>
            <input
              id="leave-date"
              type="date"
              value={leaveDate}
              onChange={(e) => setLeaveDate(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="leave-type" className="text-sm font-medium text-gray-700">
              種別
            </label>
            <select
              id="leave-type"
              value={leaveType}
              onChange={(e) => setLeaveType(e.target.value as LeaveType)}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm"
            >
              {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="leave-reason" className="text-sm font-medium text-gray-700">
              理由（任意）
            </label>
            <input
              id="leave-reason"
              type="text"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 text-sm"
            />
          </div>

          {error && (
            <p className="text-sm text-red-600">{error}</p>
          )}

          <Button onClick={handleSubmit} disabled={submitting}>
            申請
          </Button>
        </div>
      </Card>

      {balance && balance.balances.length > 0 && (
        <Card title="付与明細">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">付与日</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">失効日</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">付与</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">使用</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">残</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {balance.balances.map((b) => (
                  <tr key={b.grantDate}>
                    <td className="px-3 py-2">{b.grantDate}</td>
                    <td className="px-3 py-2">{b.expiryDate}</td>
                    <td className="px-3 py-2">{b.grantedDays}</td>
                    <td className="px-3 py-2">{b.usedDays}</td>
                    <td className="px-3 py-2">{b.remainingDays}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
