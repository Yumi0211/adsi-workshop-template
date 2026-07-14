"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { PaginatedResponse } from "@/types";

interface AlertItem {
  id: number;
  employeeId: number;
  employeeName: string;
  type: string;
  message: string;
  createdAt: string;
  acknowledged: boolean;
}

export default function AdminAlertsPage() {
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [error, setError] = useState("");

  const fetchAlerts = useCallback(async () => {
    try {
      const data = await apiClient.get<PaginatedResponse<AlertItem>>("/api/v1/admin/alerts");
      setAlerts(data.content);
    } catch {
      setError("アラート一覧の取得に失敗しました");
    }
  }, []);

  useEffect(() => { fetchAlerts(); }, [fetchAlerts]);

  const handleAcknowledge = async (id: number) => {
    setError("");
    try {
      await apiClient.put(`/api/v1/admin/alerts/${id}/acknowledge`);
      await fetchAlerts();
    } catch {
      setError("確認済み処理に失敗しました");
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">アラート一覧</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        {alerts.length === 0 ? (
          <p className="text-sm text-gray-500">アラートはありません</p>
        ) : (
          <ul className="divide-y divide-gray-200">
            {alerts.map((alert) => (
              <li key={alert.id} className="py-3 flex items-start justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-gray-900">{alert.employeeName}</p>
                  <p className="text-sm text-gray-600">{alert.message}</p>
                  <p className="text-xs text-gray-400 mt-1">{new Date(alert.createdAt).toLocaleString("ja-JP")}</p>
                </div>
                <div className="flex-shrink-0">
                  {alert.acknowledged ? (
                    <span className="text-xs text-green-600 font-medium">対応済み</span>
                  ) : (
                    <Button onClick={() => handleAcknowledge(alert.id)} className="text-xs px-2 py-1">確認済み</Button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
