import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Loading } from "@/components/ui/Loading";

describe("Loading", () => {
  it("デフォルトメッセージを表示する", () => {
    render(<Loading />);
    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  it("カスタムメッセージを表示する", () => {
    render(<Loading message="データを取得中..." />);
    expect(screen.getByText("データを取得中...")).toBeInTheDocument();
  });
});
