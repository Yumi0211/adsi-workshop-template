import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Header } from "@/components/layout/Header";

describe("Header", () => {
  it("ユーザー名を表示する", () => {
    render(<Header userName="田中太郎" onLogout={() => {}} onMenuToggle={() => {}} />);
    expect(screen.getByText("田中太郎")).toBeInTheDocument();
  });

  it("ログアウトボタンがある", () => {
    render(<Header userName="田中太郎" onLogout={() => {}} onMenuToggle={() => {}} />);
    expect(screen.getByRole("button", { name: "ログアウト" })).toBeInTheDocument();
  });
});
