import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { Button } from "@/components/ui/Button";

describe("Button", () => {
  it("variant=primary が正しいクラスを持つ", () => {
    render(<Button variant="primary">送信</Button>);
    const button = screen.getByRole("button", { name: "送信" });
    expect(button).toHaveClass("bg-primary-600");
  });

  it("variant=secondary が正しいクラスを持つ", () => {
    render(<Button variant="secondary">キャンセル</Button>);
    const button = screen.getByRole("button", { name: "キャンセル" });
    expect(button).toHaveClass("bg-gray-200");
  });

  it("variant=danger が正しいクラスを持つ", () => {
    render(<Button variant="danger">削除</Button>);
    const button = screen.getByRole("button", { name: "削除" });
    expect(button).toHaveClass("bg-red-600");
  });

  it("disabled 時にクリックが発火しない", async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(
      <Button disabled onClick={onClick}>
        送信
      </Button>
    );
    const button = screen.getByRole("button", { name: "送信" });
    await user.click(button);
    expect(onClick).not.toHaveBeenCalled();
  });
});
