"use client";

import { useState, useCallback } from "react";

type ToastType = "info" | "success" | "error";

interface ToastData {
  type: ToastType;
  message: string;
}

export function useToastState() {
  const [toast, setToast] = useState<ToastData | null>(null);

  const show = useCallback((data: ToastData) => {
    setToast(data);
  }, []);

  const hide = useCallback(() => {
    setToast(null);
  }, []);

  return { toast, show, hide };
}
