"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { TimeRecord, ClockType } from "@/types/time-record";

const CLOCK_TYPE_LABELS: Record<ClockType, string> = {
  CLOCK_IN: "出勤",
  CLOCK_OUT: "退勤",
  BREAK_START: "休憩開始",
  BREAK_END: "休憩終了",
};

function formatTime(datetime: string): string {
  const date = new Date(datetime);
  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

function getTodayString(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

export default function ClockPage() {
  const [records, setRecords] = useState<TimeRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchRecords = useCallback(async () => {
    try {
      const today = getTodayString();
      const data = await apiClient.get<TimeRecord[]>(`/api/v1/time-records?date=${today}`);
      setRecords(data);
    } catch {
      setError("打刻履歴の取得に失敗しました");
    }
  }, []);

  useEffect(() => { fetchRecords(); }, [fetchRecords]);

  const handleClock = async (type: ClockType) => {
    setLoading(true);
    setError("");
    try {
      await apiClient.post("/api/v1/time-records", { type, source: "WEB" });
      await fetchRecords();
    } catch {
      setError("打刻に失敗しました。もう一度お試しください");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">打刻</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        <div className="grid grid-cols-2 gap-4">
          <Button onClick={() => handleClock("CLOCK_IN")} disabled={loading} className="py-6 text-lg">出勤</Button>
          <Button onClick={() => handleClock("CLOCK_OUT")} disabled={loading} className="py-6 text-lg">退勤</Button>
          <Button variant="secondary" onClick={() => handleClock("BREAK_START")} disabled={loading} className="py-4">休憩開始</Button>
          <Button variant="secondary" onClick={() => handleClock("BREAK_END")} disabled={loading} className="py-4">休憩終了</Button>
        </div>
      </Card>
      <Card title="今日の打刻履歴">
        {records.length === 0 ? (
          <p className="text-sm text-gray-500">打刻履歴はありません</p>
        ) : (
          <ul className="space-y-2">
            {records.map((record) => (
              <li key={record.id} className="flex items-center gap-3 text-sm">
                <span className="font-mono text-gray-900">{formatTime(record.recordedAt)}</span>
                <span className="text-gray-600">{CLOCK_TYPE_LABELS[record.type]}</span>
                <span className="text-xs text-gray-400">({record.source})</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
