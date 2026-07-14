import { type ReactNode } from "react";

interface CardProps {
  title?: string;
  children: ReactNode;
  className?: string;
}

export function Card({ title, children, className = "" }: CardProps) {
  return (
    <div className={`bg-white rounded-lg shadow-sm border border-gray-200 p-4 ${className}`}>
      {title && (
        <h3 className="text-lg font-semibold text-gray-900 mb-3">{title}</h3>
      )}
      {children}
    </div>
  );
}
