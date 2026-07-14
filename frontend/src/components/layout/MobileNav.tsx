"use client";

import Link from "next/link";
import type { Role } from "@/types";
import { NAV_ITEMS } from "@/lib/constants";

interface MobileNavProps {
  role: Role;
  currentPath: string;
  open: boolean;
  onClose: () => void;
}

export function MobileNav({ role, currentPath, open, onClose }: MobileNavProps) {
  if (!open) return null;

  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <div className="fixed inset-0 z-40 lg:hidden">
      <div className="fixed inset-0 bg-black/50" onClick={onClose} aria-hidden="true" />
      <div className="fixed inset-y-0 left-0 w-64 bg-white shadow-xl">
        <div className="flex items-center justify-between p-4 border-b">
          <span className="font-semibold text-gray-900">メニュー</span>
          <button onClick={onClose} className="p-2 rounded-md hover:bg-gray-100" aria-label="閉じる">
            &times;
          </button>
        </div>
        <nav className="py-4">
          <ul className="space-y-1">
            {visibleItems.map((item) => {
              const isActive = currentPath === item.href;
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    onClick={onClose}
                    className={`block px-4 py-2 text-sm mx-2 rounded-md ${
                      isActive
                        ? "bg-primary-100 text-primary-700 font-medium"
                        : "text-gray-700 hover:bg-gray-100"
                    }`}
                  >
                    {item.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </div>
    </div>
  );
}
