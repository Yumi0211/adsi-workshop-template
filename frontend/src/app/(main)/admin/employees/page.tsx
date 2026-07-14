"use client";

import { useState, useEffect, useCallback } from "react";
import { apiClient } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Modal } from "@/components/ui/Modal";
import type { Employee, EmployeeCreateRequest } from "@/types/employee";
import type { PaginatedResponse, Role } from "@/types";

const ROLE_LABELS: Record<Role, string> = { EMPLOYEE: "一般", APPROVER: "承認者", ADMIN: "管理者" };

export default function AdminEmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [error, setError] = useState("");
  const [formError, setFormError] = useState("");
  const [form, setForm] = useState<EmployeeCreateRequest>({ employeeCode: "", name: "", email: "", role: "EMPLOYEE", hireDate: "", departmentIds: [] });

  const fetchEmployees = useCallback(async () => {
    try {
      const data = await apiClient.get<PaginatedResponse<Employee>>("/api/v1/admin/employees");
      setEmployees(data.content);
    } catch { setError("社員一覧の取得に失敗しました"); }
  }, []);

  useEffect(() => { fetchEmployees(); }, [fetchEmployees]);

  const validateForm = (): string | null => {
    if (!form.employeeCode.trim()) return "社員コードを入力してください";
    if (!form.name.trim()) return "氏名を入力してください";
    if (!form.email.trim()) return "メールを入力してください";
    if (!form.hireDate) return "入社日を入力してください";
    return null;
  };

  const handleCreate = async () => {
    setFormError("");
    const validationError = validateForm();
    if (validationError) { setFormError(validationError); return; }
    try {
      await apiClient.post("/api/v1/admin/employees", form);
      setModalOpen(false);
      setForm({ employeeCode: "", name: "", email: "", role: "EMPLOYEE", hireDate: "", departmentIds: [] });
      await fetchEmployees();
    } catch { setFormError("社員登録に失敗しました"); }
  };

  const handleDeactivate = async (id: number) => {
    setError("");
    try {
      await apiClient.put(`/api/v1/admin/employees/${id}/deactivate`);
      await fetchEmployees();
    } catch { setError("無効化に失敗しました"); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">社員管理</h2>
        <Button onClick={() => setModalOpen(true)}>新規登録</Button>
      </div>
      {error && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{error}</div>}
      <Card>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50"><tr><th className="px-3 py-2 text-left font-medium text-gray-500">社員コード</th><th className="px-3 py-2 text-left font-medium text-gray-500">氏名</th><th className="px-3 py-2 text-left font-medium text-gray-500">メール</th><th className="px-3 py-2 text-left font-medium text-gray-500">ロール</th><th className="px-3 py-2 text-left font-medium text-gray-500">部門</th><th className="px-3 py-2 text-left font-medium text-gray-500">操作</th></tr></thead>
            <tbody className="divide-y divide-gray-200">
              {employees.map((emp) => (
                <tr key={emp.id}>
                  <td className="px-3 py-2 font-mono">{emp.employeeCode}</td>
                  <td className="px-3 py-2">{emp.name}</td>
                  <td className="px-3 py-2">{emp.email}</td>
                  <td className="px-3 py-2">{ROLE_LABELS[emp.role]}</td>
                  <td className="px-3 py-2">{emp.departments.map((d) => d.departmentName).join(", ")}</td>
                  <td className="px-3 py-2">{emp.active && <Button variant="danger" onClick={() => handleDeactivate(emp.id)} className="text-xs px-2 py-1">無効化</Button>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="社員登録">
        <div className="space-y-4">
          {formError && <div role="alert" className="px-4 py-3 rounded-md border bg-red-50 text-red-800 border-red-200">{formError}</div>}
          <div className="flex flex-col gap-1"><label htmlFor="emp-code" className="text-sm font-medium text-gray-700">社員コード</label><input id="emp-code" type="text" value={form.employeeCode} onChange={(e) => setForm({ ...form, employeeCode: e.target.value })} className="border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div className="flex flex-col gap-1"><label htmlFor="emp-name" className="text-sm font-medium text-gray-700">氏名</label><input id="emp-name" type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div className="flex flex-col gap-1"><label htmlFor="emp-email" className="text-sm font-medium text-gray-700">メール</label><input id="emp-email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} className="border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div className="flex flex-col gap-1"><label htmlFor="emp-role" className="text-sm font-medium text-gray-700">ロール</label><select id="emp-role" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as Role })} className="border border-gray-300 rounded-md px-3 py-2 text-sm">{Object.entries(ROLE_LABELS).map(([value, label]) => (<option key={value} value={value}>{label}</option>))}</select></div>
          <div className="flex flex-col gap-1"><label htmlFor="emp-hire-date" className="text-sm font-medium text-gray-700">入社日</label><input id="emp-hire-date" type="date" value={form.hireDate} onChange={(e) => setForm({ ...form, hireDate: e.target.value })} className="border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div className="flex gap-2 justify-end"><Button variant="secondary" onClick={() => setModalOpen(false)}>キャンセル</Button><Button onClick={handleCreate}>登録</Button></div>
        </div>
      </Modal>
    </div>
  );
}
