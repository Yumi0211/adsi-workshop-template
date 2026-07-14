import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { Toast } from "@/components/ui/Toast";

describe("Toast", () => {
  it("メッセージを表示する", () => {
    render(<Toast type="success" message="保存しました" onClose={() => {}} />);
    expect(screen.getByText("保存しました")).toBeInTheDocument();
  });

  it("閉じるボタンで onClose が呼ばれる", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    render(<Toast type="error" message="エラー" onClose={onClose} />);

    await user.click(screen.getByRole("button", { name: "閉じる" }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("type=success が正しいスタイルを持つ", () => {
    render(<Toast type="success" message="成功" onClose={() => {}} />);
    expect(screen.getByRole("status")).toHaveClass("bg-green-600");
  });
});
