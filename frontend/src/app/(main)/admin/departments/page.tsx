"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import type { DepartmentNode } from "@/types/employee";

function DepartmentTree({ nodes, depth = 0 }: { nodes: DepartmentNode[]; depth?: number }) {
  return (
    <ul className={depth > 0 ? "ml-6 border-l border-gray-200 pl-4" : ""}>
      {nodes.map((node) => (
        <li key={node.id} className="py-1">
          <span className="text-sm text-gray-900">{node.name}</span>
          {!node.active && <span className="ml-2 text-xs text-red-500">(無効)</span>}
          {node.children.length > 0 && <DepartmentTree nodes={node.children} depth={depth + 1} />}
        </li>
      ))}
    </ul>
  );
}

export default function AdminDepartmentsPage() {
  const [departments, setDepartments] = useState<DepartmentNode[]>([]);
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState<number | null>(null);
  const [error, setError] = useState("");

  const fetchDepartments = useCallback(async () => {
    try {
      const data = await apiClient.get<DepartmentNode[]>("/api/v1/admin/departments");
      setDepartments(data);
    } catch { setError("部門一覧の取得に失敗しました"); }
  }, []);

  useEffect(() => { fetchDepartments(); }, [fetchDepartments]);

  const flattenDepartments = (nodes: DepartmentNode[]): { id: number; name: string }[] => {
    const result: { id: number; name: string }[] = [];
    const traverse = (items: DepartmentNode[]) => { for (const item of items) { result.push({ id: item.id, name: item.name }); traverse(item.children); } };
    traverse(nodes);
    return result;
  };

  const handleCreate = async () => {
    setError("");
    if (!name.trim()) { setError("部門名を入力してください"); return; }
    try {
      await apiClient.post("/api/v1/admin/departments", { name, parentId });
      setName("");
      setParentId(null);
      await fetchDepartments();
    } catch { setError("部門の追加に失敗しました"); }
  };

  const flatList = flattenDepartments(departments);

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">部門管理</h2>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card title="部門ツリー">
        {departments.length === 0 ? <p className="text-sm text-gray-500">部門が登録されていません</p> : <DepartmentTree nodes={departments} />}
      </Card>
      <Card title="部門追加">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex flex-col gap-1 flex-1">
            <label htmlFor="dept-name" className="text-sm font-medium text-gray-700">部門名</label>
            <input id="dept-name" type="text" value={name} onChange={(e) => setName(e.target.value)} className="border border-gray-300 rounded-md px-3 py-2 text-sm" />
          </div>
          <div className="flex flex-col gap-1 flex-1">
            <label htmlFor="dept-parent" className="text-sm font-medium text-gray-700">親部門（任意）</label>
            <select id="dept-parent" value={parentId ?? ""} onChange={(e) => setParentId(e.target.value ? Number(e.target.value) : null)} className="border border-gray-300 rounded-md px-3 py-2 text-sm">
              <option value="">なし（ルート）</option>
              {flatList.map((d) => (<option key={d.id} value={d.id}>{d.name}</option>))}
            </select>
          </div>
          <div className="flex items-end"><Button onClick={handleCreate}>追加</Button></div>
        </div>
      </Card>
    </div>
  );
}
