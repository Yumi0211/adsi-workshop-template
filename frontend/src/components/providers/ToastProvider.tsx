"use client";

import { createContext, useContext, type ReactNode } from "react";
import { useToastState } from "@/hooks/useToast";
import { Toast } from "@/components/ui/Toast";

type ToastType = "info" | "success" | "error";

interface ToastContextValue {
  show: (data: { type: ToastType; message: string }) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: ReactNode }) {
  const { toast, show, hide } = useToastState();

  return (
    <ToastContext.Provider value={{ show }}>
      {children}
      {toast && <Toast type={toast.type} message={toast.message} onClose={hide} />}
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}
