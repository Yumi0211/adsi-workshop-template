import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { withBasePath, apiClient } from "@/lib/api-client";

describe("withBasePath", () => {
  it("BASE_PATH 未設定時はパスをそのまま返す", () => {
    const result = withBasePath("/api/v1/employees");
    expect(result).toBe("/api/v1/employees");
  });

  it("withBasePath はプレフィックスを付与する構造になっている", () => {
    expect(typeof withBasePath).toBe("function");
    expect(withBasePath("/test")).toMatch(/\/test$/);
  });
});

describe("apiClient", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("get: 正常レスポンスを返す", async () => {
    const mockData = { id: 1, name: "田中太郎" };
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify(mockData), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    );

    const result = await apiClient.get("/api/v1/employees/1");
    expect(result).toEqual(mockData);
    expect(fetch).toHaveBeenCalledWith(
      "/api/v1/employees/1",
      expect.objectContaining({ method: "GET", credentials: "include" })
    );
  });

  it("get: 401 時に認証エラーを投げる", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ status: 401, detail: "Unauthorized" }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      })
    );

    await expect(apiClient.get("/api/v1/auth/me")).rejects.toThrow("認証エラー");
  });

  it("get: 403 時に権限エラーを投げる", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ status: 403, detail: "Forbidden" }), {
        status: 403,
        headers: { "Content-Type": "application/json" },
      })
    );

    await expect(apiClient.get("/api/v1/admin/employees")).rejects.toThrow(
      "権限がありません"
    );
  });

  it("get: 500 時にサーバーエラーメッセージを投げる", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(
        JSON.stringify({ status: 500, detail: "Internal Server Error" }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      )
    );

    await expect(apiClient.get("/api/v1/employees")).rejects.toThrow(
      "Internal Server Error"
    );
  });

  it("get: エラーレスポンスが不正な形式の場合デフォルトメッセージを使う", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response("not json", { status: 500 })
    );

    await expect(apiClient.get("/api/v1/employees")).rejects.toThrow(
      "エラーが発生しました"
    );
  });

  it("post: JSON body を送信する", async () => {
    const requestBody = { type: "CLOCK_IN", source: "WEB" };
    const responseData = { id: 1, type: "CLOCK_IN" };
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify(responseData), {
        status: 201,
        headers: { "Content-Type": "application/json" },
      })
    );

    const result = await apiClient.post("/api/v1/time-records", requestBody);
    expect(result).toEqual(responseData);
    expect(fetch).toHaveBeenCalledWith(
      "/api/v1/time-records",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          "Content-Type": "application/json",
        }),
        body: JSON.stringify(requestBody),
        credentials: "include",
      })
    );
  });

  it("post: body なしで送信できる", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ success: true }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    );

    await apiClient.post("/api/v1/auth/logout");
    expect(fetch).toHaveBeenCalledWith(
      "/api/v1/auth/logout",
      expect.objectContaining({ method: "POST", body: undefined })
    );
  });

  it("put: JSON body を送信する", async () => {
    const requestBody = { name: "更新太郎" };
    const responseData = { id: 1, name: "更新太郎" };
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify(responseData), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    );

    const result = await apiClient.put("/api/v1/employees/1", requestBody);
    expect(result).toEqual(responseData);
    expect(fetch).toHaveBeenCalledWith(
      "/api/v1/employees/1",
      expect.objectContaining({ method: "PUT", credentials: "include" })
    );
  });

  it("delete: 204 レスポンスを処理する", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(null, { status: 204 })
    );

    const result = await apiClient.delete("/api/v1/calendars/1");
    expect(result).toBeUndefined();
    expect(fetch).toHaveBeenCalledWith(
      "/api/v1/calendars/1",
      expect.objectContaining({ method: "DELETE", credentials: "include" })
    );
  });
});
