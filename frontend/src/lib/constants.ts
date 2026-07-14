import type { NavItem } from "@/types";

export const NAV_ITEMS: NavItem[] = [
  { label: "ダッシュボード", href: "/", roles: ["EMPLOYEE", "APPROVER", "ADMIN"] },
  { label: "打刻", href: "/clock", roles: ["EMPLOYEE", "APPROVER", "ADMIN"] },
  { label: "月次勤怠", href: "/attendance", roles: ["EMPLOYEE", "APPROVER", "ADMIN"] },
  { label: "有給休暇", href: "/leaves", roles: ["EMPLOYEE", "APPROVER", "ADMIN"] },
  { label: "申請一覧", href: "/my-requests", roles: ["EMPLOYEE", "APPROVER", "ADMIN"] },
  { label: "承認一覧", href: "/approvals", roles: ["APPROVER", "ADMIN"] },
  { label: "部門勤怠", href: "/team/attendance", roles: ["APPROVER", "ADMIN"] },
  { label: "社員管理", href: "/admin/employees", roles: ["ADMIN"] },
  { label: "部門管理", href: "/admin/departments", roles: ["ADMIN"] },
  { label: "カレンダー設定", href: "/admin/calendars", roles: ["ADMIN"] },
  { label: "レポート", href: "/admin/reports", roles: ["ADMIN"] },
  { label: "アラート", href: "/admin/alerts", roles: ["ADMIN"] },
];
