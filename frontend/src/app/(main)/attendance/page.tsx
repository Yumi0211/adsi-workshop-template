"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { MonthlyAttendance } from "@/types/attendance";

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${String(m).padStart(2, "0")}m`;
}

function formatTime(datetime: string | null): string {
  if (!datetime) return "-";
  const d = new Date(datetime);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return `${String(d.getMonth() + 1).padStart(2, "0")}/${String(d.getDate()).padStart(2, "0")}`;
}

export default function AttendancePage() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [data, setData] = useState<MonthlyAttendance | null>(null);
  const [error, setError] = useState("");

  const fetchData = useCallback(async () => {
    setError("");
    try {
      const result = await apiClient.get<MonthlyAttendance>(`/api/v1/attendances/monthly?year=${year}&month=${month}`);
      setData(result);
    } catch {
      setError("勤怠データの取得に失敗しました");
    }
  }, [year, month]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handlePrevMonth = () => { if (month === 1) { setYear(year - 1); setMonth(12); } else { setMonth(month - 1); } };
  const handleNextMonth = () => { if (month === 12) { setYear(year + 1); setMonth(1); } else { setMonth(month + 1); } };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">月次勤怠</h2>
        <div className="flex items-center gap-2">
          <Button variant="secondary" onClick={handlePrevMonth}>前月</Button>
          <span className="text-sm font-medium text-gray-700">{year}年{month}月</span>
          <Button variant="secondary" onClick={handleNextMonth}>次月</Button>
        </div>
      </div>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      {data && (
        <>
          <Card>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">日付</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">出勤</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">退勤</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">休憩</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">勤務</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">残業</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">状態</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {data.records.map((record) => (
                    <tr key={record.date}>
                      <td className="px-3 py-2 font-mono">{formatDate(record.date)}</td>
                      <td className="px-3 py-2">{formatTime(record.clockIn)}</td>
                      <td className="px-3 py-2">{formatTime(record.clockOut)}</td>
                      <td className="px-3 py-2">{record.breakMinutes > 0 ? formatMinutes(record.breakMinutes) : "-"}</td>
                      <td className="px-3 py-2">{record.workingMinutes > 0 ? formatMinutes(record.workingMinutes) : "-"}</td>
                      <td className="px-3 py-2">{record.overtimeMinutes > 0 ? formatMinutes(record.overtimeMinutes) : "-"}</td>
                      <td className="px-3 py-2">{record.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
          <Card title="月間サマリー">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div><span className="text-gray-500">勤務日数</span><p className="font-semibold">{data.summary.workingDays}日</p></div>
              <div><span className="text-gray-500">勤務時間</span><p className="font-semibold">{formatMinutes(data.summary.totalWorkingMinutes)}</p></div>
              <div><span className="text-gray-500">残業</span><p className="font-semibold">{formatMinutes(data.summary.totalOvertimeMinutes)}</p></div>
              <div><span className="text-gray-500">深夜</span><p className="font-semibold">{formatMinutes(data.summary.totalNightMinutes)}</p></div>
            </div>
          </Card>
        </>
      )}
    </div>
  );
}
