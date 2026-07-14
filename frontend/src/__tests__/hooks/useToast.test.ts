import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useToastState } from "@/hooks/useToast";

describe("useToastState", () => {
  it("show でトーストを表示し、hide で非表示にする", () => {
    const { result } = renderHook(() => useToastState());

    expect(result.current.toast).toBeNull();

    act(() => {
      result.current.show({ type: "success", message: "保存しました" });
    });
    expect(result.current.toast).toEqual({ type: "success", message: "保存しました" });

    act(() => {
      result.current.hide();
    });
    expect(result.current.toast).toBeNull();
  });
});
