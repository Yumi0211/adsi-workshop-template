import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { Card } from "@/components/ui/Card";

describe("Card", () => {
  it("title と children を描画する", () => {
    render(
      <Card title="今月のサマリー">
        <p>勤務日数: 10日</p>
      </Card>
    );
    expect(screen.getByText("今月のサマリー")).toBeInTheDocument();
    expect(screen.getByText("勤務日数: 10日")).toBeInTheDocument();
  });
});
