import type { ApiError } from "@/types";

const BASE_PATH = process.env.NEXT_PUBLIC_BASE_PATH ?? "";

export function withBasePath(path: string): string {
  return `${BASE_PATH}${path}`;
}

class ApiClientError extends Error {
  constructor(
    message: string,
    public status: number,
    public apiError?: ApiError
  ) {
    super(message);
    this.name = "ApiClientError";
  }
}

function isApiError(value: unknown): value is ApiError {
  return (
    typeof value === "object" &&
    value !== null &&
    "status" in value &&
    "detail" in value
  );
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    throw new ApiClientError("認証エラー", 401);
  }

  if (response.status === 403) {
    throw new ApiClientError("権限がありません", 403);
  }

  if (!response.ok) {
    const errorBody: unknown = await response.json().catch(() => null);
    const apiError = isApiError(errorBody) ? errorBody : undefined;
    throw new ApiClientError(
      apiError?.detail ?? "エラーが発生しました",
      response.status,
      apiError
    );
  }

  if (response.status === 204) {
    return undefined as unknown as T;
  }

  return response.json() as Promise<T>;
}

export const apiClient = {
  async get<T>(path: string): Promise<T> {
    const url = withBasePath(path);
    const response = await fetch(url, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
    });
    return handleResponse<T>(response);
  },

  async post<T>(path: string, body?: unknown): Promise<T> {
    const url = withBasePath(path);
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: body ? JSON.stringify(body) : undefined,
    });
    return handleResponse<T>(response);
  },

  async put<T>(path: string, body?: unknown): Promise<T> {
    const url = withBasePath(path);
    const response = await fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: body ? JSON.stringify(body) : undefined,
    });
    return handleResponse<T>(response);
  },

  async delete<T>(path: string): Promise<T> {
    const url = withBasePath(path);
    const response = await fetch(url, {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
    });
    return handleResponse<T>(response);
  },
};
