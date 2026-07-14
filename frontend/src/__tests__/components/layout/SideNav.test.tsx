import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { SideNav } from "@/components/layout/SideNav";

describe("SideNav", () => {
  it("ロールに応じたメニュー項目を表示する（ADMIN）", () => {
    render(<SideNav role="ADMIN" currentPath="/" />);
    expect(screen.getByText("ダッシュボード")).toBeInTheDocument();
    expect(screen.getByText("社員管理")).toBeInTheDocument();
    expect(screen.getByText("承認一覧")).toBeInTheDocument();
  });

  it("EMPLOYEE ロールには管理者メニューが見えない", () => {
    render(<SideNav role="EMPLOYEE" currentPath="/" />);
    expect(screen.getByText("ダッシュボード")).toBeInTheDocument();
    expect(screen.queryByText("社員管理")).not.toBeInTheDocument();
    expect(screen.queryByText("承認一覧")).not.toBeInTheDocument();
  });
});
