type AlertType = "info" | "warning" | "error" | "success";

interface AlertProps {
  type: AlertType;
  message: string;
}

const TYPE_CLASSES: Record<AlertType, string> = {
  info: "bg-blue-50 text-blue-800 border-blue-200",
  warning: "bg-yellow-50 text-yellow-800 border-yellow-200",
  error: "bg-red-50 text-red-800 border-red-200",
  success: "bg-green-50 text-green-800 border-green-200",
};

export function Alert({ type, message }: AlertProps) {
  return (
    <div
      role="alert"
      className={`px-4 py-3 rounded-md border ${TYPE_CLASSES[type]}`}
    >
      {message}
    </div>
  );
}
