import type { Role } from "@/types";

export interface Employee {
  id: number;
  employeeCode: string;
  name: string;
  email: string;
  role: Role;
  hireDate: string;
  active: boolean;
  departments: EmployeeDepartment[];
}

export interface EmployeeDepartment {
  departmentId: number;
  departmentName: string;
  isPrimary: boolean;
}

export interface EmployeeCreateRequest {
  employeeCode: string;
  name: string;
  email: string;
  role: Role;
  hireDate: string;
  departmentIds: { departmentId: number; isPrimary: boolean }[];
}

export interface DepartmentNode {
  id: number;
  name: string;
  parentId: number | null;
  active: boolean;
  children: DepartmentNode[];
}

export interface DepartmentCreateRequest {
  name: string;
  parentId: number | null;
}
