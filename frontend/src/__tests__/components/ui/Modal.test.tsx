import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Modal } from "@/components/ui/Modal";

describe("Modal", () => {
  it("open=true 時に表示される", () => {
    render(
      <Modal open={true} onClose={() => {}} title="確認">
        <p>本当に削除しますか？</p>
      </Modal>
    );
    expect(screen.getByText("確認")).toBeInTheDocument();
    expect(screen.getByText("本当に削除しますか？")).toBeInTheDocument();
  });

  it("open=false 時に非表示になる", () => {
    render(
      <Modal open={false} onClose={() => {}} title="確認">
        <p>本当に削除しますか？</p>
      </Modal>
    );
    expect(screen.queryByText("確認")).not.toBeInTheDocument();
  });
});
