import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Input } from "@/components/ui/Input";

describe("Input", () => {
  it("label と input が紐づく", () => {
    render(<Input label="メールアドレス" name="email" />);
    const input = screen.getByLabelText("メールアドレス");
    expect(input).toBeInTheDocument();
  });

  it("エラーメッセージを表示する", () => {
    render(<Input label="メール" name="email" error="必須項目です" />);
    expect(screen.getByText("必須項目です")).toBeInTheDocument();
  });
});
