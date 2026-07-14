import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Alert } from "@/components/ui/Alert";

describe("Alert", () => {
  it("type=info が正しいスタイルを持つ", () => {
    render(<Alert type="info" message="情報メッセージ" />);
    const alert = screen.getByRole("alert");
    expect(alert).toHaveClass("bg-blue-50");
    expect(screen.getByText("情報メッセージ")).toBeInTheDocument();
  });

  it("type=warning が正しいスタイルを持つ", () => {
    render(<Alert type="warning" message="警告メッセージ" />);
    const alert = screen.getByRole("alert");
    expect(alert).toHaveClass("bg-yellow-50");
  });

  it("type=error が正しいスタイルを持つ", () => {
    render(<Alert type="error" message="エラーメッセージ" />);
    const alert = screen.getByRole("alert");
    expect(alert).toHaveClass("bg-red-50");
  });

  it("type=success が正しいスタイルを持つ", () => {
    render(<Alert type="success" message="成功メッセージ" />);
    const alert = screen.getByRole("alert");
    expect(alert).toHaveClass("bg-green-50");
  });
});
