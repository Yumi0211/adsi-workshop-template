"use client";

import { type InputHTMLAttributes } from "react";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  name: string;
  error?: string;
}

export function Input({ label, name, error, className = "", ...props }: InputProps) {
  const id = `input-${name}`;

  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium text-gray-700">
        {label}
      </label>
      <input
        id={id}
        name={name}
        className={`border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 ${
          error ? "border-red-500" : "border-gray-300"
        } ${className}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
        {...props}
      />
      {error && (
        <p id={`${id}-error`} className="text-sm text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}
