export type Role = "EMPLOYEE" | "APPROVER" | "ADMIN";

export interface User {
  employeeId: number;
  employeeCode: string;
  name: string;
  email: string;
  role: Role;
  departments: Department[];
}

export interface Department {
  id: number;
  name: string;
  isPrimary: boolean;
}

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

export interface NavItem {
  label: string;
  href: string;
  roles: Role[];
  icon?: string;
}
