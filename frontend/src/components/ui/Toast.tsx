"use client";

type ToastType = "info" | "success" | "error";

interface ToastProps {
  type: ToastType;
  message: string;
  onClose: () => void;
}

const TYPE_CLASSES: Record<ToastType, string> = {
  info: "bg-blue-600",
  success: "bg-green-600",
  error: "bg-red-600",
};

export function Toast({ type, message, onClose }: ToastProps) {
  return (
    <div
      className={`fixed bottom-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-md text-white shadow-lg ${TYPE_CLASSES[type]}`}
      role="status"
    >
      <span>{message}</span>
      <button onClick={onClose} className="text-white/80 hover:text-white" aria-label="閉じる">
        &times;
      </button>
    </div>
  );
}
