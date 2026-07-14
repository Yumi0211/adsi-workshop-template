-- ローカル開発用テストデータ
INSERT INTO employees (employee_code, name, email, role, hire_date, active, version) VALUES
('EMP100', '佐藤花子', 'sato@example.com', 'EMPLOYEE', '2020-04-01', true, 0);

INSERT INTO departments (name, level, parent_id, active, version) VALUES
('開発部', 'DIVISION', NULL, true, 0);

INSERT INTO employee_departments (employee_id, department_id, is_primary, start_date, version) VALUES
((SELECT id FROM employees WHERE employee_code = 'EMP100'), (SELECT id FROM departments WHERE name = '開発部'), true, '2020-04-01', 0);

INSERT INTO leave_balances (employee_id, fiscal_year, granted_days, used_days, carried_over_days, grant_date, expiry_date, version) VALUES
((SELECT id FROM employees WHERE employee_code = 'EMP100'), 2026, 20.0, 5.0, 0, '2026-04-01', '2028-03-31', 0);
