import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Table } from "@/components/ui/Table";

describe("Table", () => {
  it("ヘッダーとデータ行を描画する", () => {
    const columns = [
      { key: "name", header: "名前" },
      { key: "email", header: "メール" },
    ];
    const data = [
      { name: "田中太郎", email: "tanaka@example.com" },
      { name: "鈴木花子", email: "suzuki@example.com" },
    ];

    render(<Table columns={columns} data={data} />);

    expect(screen.getByText("名前")).toBeInTheDocument();
    expect(screen.getByText("メール")).toBeInTheDocument();
    expect(screen.getByText("田中太郎")).toBeInTheDocument();
    expect(screen.getByText("suzuki@example.com")).toBeInTheDocument();
  });
});
