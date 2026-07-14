import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { MobileNav } from "@/components/layout/MobileNav";

describe("MobileNav", () => {
  it("open=true 時にメニューを表示する", () => {
    render(
      <MobileNav role="EMPLOYEE" currentPath="/" open={true} onClose={() => {}} />
    );
    expect(screen.getByText("ダッシュボード")).toBeInTheDocument();
    expect(screen.getByText("メニュー")).toBeInTheDocument();
  });

  it("open=false 時に何も表示しない", () => {
    render(
      <MobileNav role="EMPLOYEE" currentPath="/" open={false} onClose={() => {}} />
    );
    expect(screen.queryByText("メニュー")).not.toBeInTheDocument();
  });

  it("閉じるボタンで onClose が呼ばれる", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    render(
      <MobileNav role="ADMIN" currentPath="/" open={true} onClose={onClose} />
    );

    await user.click(screen.getByRole("button", { name: "閉じる" }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("EMPLOYEE ロールに管理者メニューが見えない", () => {
    render(
      <MobileNav role="EMPLOYEE" currentPath="/" open={true} onClose={() => {}} />
    );
    expect(screen.queryByText("社員管理")).not.toBeInTheDocument();
  });
});
