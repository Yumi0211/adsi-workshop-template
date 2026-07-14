"use client";

import { useState } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";

export default function CorrectionsNewPage() {
  const [targetDate, setTargetDate] = useState("");
  const [clockIn, setClockIn] = useState("");
  const [clockOut, setClockOut] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    setError("");
    setSuccess(false);
    if (!targetDate) { setError("対象日を入力してください"); return; }
    if (!reason.trim()) { setError("理由を入力してください"); return; }

    setSubmitting(true);
    try {
      await apiClient.post("/api/v1/time-corrections", {
        targetDate,
        correctedClockIn: clockIn || null,
        correctedClockOut: clockOut || null,
        reason,
      });
      setSuccess(true);
      setTargetDate("");
      setClockIn("");
      setClockOut("");
      setReason("");
    } catch {
      setError("打刻修正申請に失敗しました");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">打刻修正申請</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      {success && <div role="alert" className="px-4 py-3 rounded-md border bg-green-50 text-green-800 border-green-200">申請を送信しました</div>}

      <Card>
        <div className="space-y-4 max-w-md">
          <div className="flex flex-col gap-1">
            <label htmlFor="target-date" className="text-sm font-medium text-gray-700">対象日</label>
            <input id="target-date" type="date" value={targetDate} onChange={(e) => setTargetDate(e.target.value)} className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          </div>
          <div className="flex flex-col gap-1">
            <label htmlFor="clock-in" className="text-sm font-medium text-gray-700">修正出勤時刻</label>
            <input id="clock-in" type="time" value={clockIn} onChange={(e) => setClockIn(e.target.value)} className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          </div>
          <div className="flex flex-col gap-1">
            <label htmlFor="clock-out" className="text-sm font-medium text-gray-700">修正退勤時刻</label>
            <input id="clock-out" type="time" value={clockOut} onChange={(e) => setClockOut(e.target.value)} className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          </div>
          <div className="flex flex-col gap-1">
            <label htmlFor="reason" className="text-sm font-medium text-gray-700">理由</label>
            <textarea id="reason" value={reason} onChange={(e) => setReason(e.target.value)} rows={3} className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          </div>
          <Button onClick={handleSubmit} disabled={submitting}>申請</Button>
        </div>
      </Card>
    </div>
  );
}
