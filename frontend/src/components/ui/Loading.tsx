interface LoadingProps {
  message?: string;
}

export function Loading({ message = "読み込み中..." }: LoadingProps) {
  return (
    <div className="flex items-center justify-center p-8">
      <div className="flex items-center gap-3">
        <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary-600 border-t-transparent" />
        <span className="text-sm text-gray-600">{message}</span>
      </div>
    </div>
  );
}
