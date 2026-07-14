"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { apiClient } from "@/lib/api-client";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";

interface TeamMemberAttendance {
  employeeId: number;
  employeeName: string;
  workingDays: number;
  totalWorkingMinutes: number;
  totalOvertimeMinutes: number;
}

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${String(m).padStart(2, "0")}m`;
}

export default function TeamAttendancePage() {
  const { user } = useAuth();
  const router = useRouter();
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [members, setMembers] = useState<TeamMemberAttendance[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (user && user.role === "EMPLOYEE") {
      router.replace("/");
    }
  }, [user, router]);

  const primaryDept = user?.departments?.find((d) => d.isPrimary);
  const departmentId = primaryDept?.id ?? user?.departments?.[0]?.id;

  const fetchData = useCallback(async () => {
    if (!departmentId) return;
    setError("");
    try {
      const data = await apiClient.get<TeamMemberAttendance[]>(
        `/api/v1/departments/${departmentId}/attendances/monthly?year=${year}&month=${month}`
      );
      setMembers(data);
    } catch {
      setError("部門勤怠データの取得に失敗しました");
    }
  }, [departmentId, year, month]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handlePrevMonth = () => { if (month === 1) { setYear(year - 1); setMonth(12); } else { setMonth(month - 1); } };
  const handleNextMonth = () => { if (month === 12) { setYear(year + 1); setMonth(1); } else { setMonth(month + 1); } };

  if (user && user.role === "EMPLOYEE") return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">部門勤怠</h2>
        <div className="flex items-center gap-2">
          <Button variant="secondary" onClick={handlePrevMonth}>前月</Button>
          <span className="text-sm font-medium text-gray-700">{year}年{month}月</span>
          <Button variant="secondary" onClick={handleNextMonth}>次月</Button>
        </div>
      </div>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        {members.length === 0 ? (
          <p className="text-sm text-gray-500">データがありません</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">氏名</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">勤務日数</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">勤務時間</th>
                  <th className="px-3 py-2 text-left font-medium text-gray-500">残業</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {members.map((m) => (
                  <tr key={m.employeeId}>
                    <td className="px-3 py-2">{m.employeeName}</td>
                    <td className="px-3 py-2">{m.workingDays}日</td>
                    <td className="px-3 py-2">{formatMinutes(m.totalWorkingMinutes)}</td>
                    <td className="px-3 py-2">{formatMinutes(m.totalOvertimeMinutes)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
