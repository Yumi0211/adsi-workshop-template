"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { TimeRecord, ClockType } from "@/types/time-record";
import type { AttendanceSummary } from "@/types/attendance";
import type { AlertItem } from "@/types/alert";

const CLOCK_TYPE_LABELS: Record<ClockType, string> = {
  CLOCK_IN: "出勤", CLOCK_OUT: "退勤", BREAK_START: "休憩開始", BREAK_END: "休憩終了",
};

function formatTime(datetime: string): string {
  const d = new Date(datetime);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${String(m).padStart(2, "0")}m`;
}

function getTodayString(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [records, setRecords] = useState<TimeRecord[]>([]);
  const [summary, setSummary] = useState<AttendanceSummary | null>(null);
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchData = useCallback(async () => {
    try {
      const today = getTodayString();
      const now = new Date();
      const year = now.getFullYear();
      const month = now.getMonth() + 1;

      const [recordsData, monthlyData] = await Promise.all([
        apiClient.get<TimeRecord[]>(`/api/v1/time-records?date=${today}`),
        apiClient.get<{ summary: AttendanceSummary }>(`/api/v1/attendances/monthly?year=${year}&month=${month}`),
      ]);
      setRecords(recordsData);
      setSummary(monthlyData.summary);

      if (user?.role === "ADMIN") {
        const alertsData = await apiClient.get<{ content: AlertItem[] }>("/api/v1/admin/alerts");
        setAlerts(alertsData.content.filter((a) => !a.acknowledged).slice(0, 5));
      }
    } catch {
      setError("データの取得に失敗しました");
    }
  }, [user?.role]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleClock = async (type: ClockType) => {
    setLoading(true);
    setError("");
    try {
      await apiClient.post("/api/v1/time-records", { type, source: "WEB" });
      await fetchData();
    } catch {
      setError("打刻に失敗しました");
    } finally {
      setLoading(false);
    }
  };

  const lastRecord = records[records.length - 1];

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">ダッシュボード</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}

      <Card title="打刻">
        <div className="flex gap-3 mb-4">
          <Button onClick={() => handleClock("CLOCK_IN")} disabled={loading}>出勤</Button>
          <Button onClick={() => handleClock("CLOCK_OUT")} disabled={loading}>退勤</Button>
          <Button variant="secondary" onClick={() => handleClock("BREAK_START")} disabled={loading}>休憩開始</Button>
          <Button variant="secondary" onClick={() => handleClock("BREAK_END")} disabled={loading}>休憩終了</Button>
        </div>
        {lastRecord && (
          <p className="text-sm text-gray-600">
            最終打刻: {formatTime(lastRecord.recordedAt)} ({CLOCK_TYPE_LABELS[lastRecord.type]})
          </p>
        )}
        {records.length > 0 && (
          <ul className="mt-2 space-y-1">
            {records.map((r) => (
              <li key={r.id} className="text-xs text-gray-500">
                {formatTime(r.recordedAt)} {CLOCK_TYPE_LABELS[r.type]}
              </li>
            ))}
          </ul>
        )}
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {summary && (
          <>
            <Card>
              <p className="text-sm text-gray-500">勤務日数</p>
              <p className="text-2xl font-bold">{summary.workingDays}日</p>
            </Card>
            <Card>
              <p className="text-sm text-gray-500">勤務時間</p>
              <p className="text-2xl font-bold">{formatMinutes(summary.totalWorkingMinutes)}</p>
            </Card>
            <Card>
              <p className="text-sm text-gray-500">残業</p>
              <p className="text-2xl font-bold text-yellow-600">{formatMinutes(summary.totalOvertimeMinutes)}</p>
            </Card>
            <Card>
              <p className="text-sm text-gray-500">深夜</p>
              <p className="text-2xl font-bold">{formatMinutes(summary.totalNightMinutes)}</p>
            </Card>
          </>
        )}
      </div>

      {alerts.length > 0 && (
        <Card title="通知">
          <ul className="space-y-2">
            {alerts.map((alert) => (
              <li key={alert.id} className="flex items-start gap-2 text-sm">
                <span className="text-yellow-500">⚠</span>
                <span className="text-gray-700">{alert.message}</span>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}
