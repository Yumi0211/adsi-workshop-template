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
      expect.objectContaining({ method: "GET" })
    );
  });

  it("get: 401 時に認証エラーを投げる", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ status: 401, detail: "Unauthorized" }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      })
    );

    await expect(apiClient.get("/api/v1/auth/me")).rejects.toThrow(
      "認証エラー"
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
      })
    );
  });
});
