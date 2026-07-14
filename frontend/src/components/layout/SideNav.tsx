"use client";

import Link from "next/link";
import type { Role } from "@/types";
import { NAV_ITEMS } from "@/lib/constants";

interface SideNavProps {
  role: Role;
  currentPath: string;
}

export function SideNav({ role, currentPath }: SideNavProps) {
  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <nav className="w-60 bg-gray-50 border-r border-gray-200 h-full overflow-y-auto">
      <ul className="py-4 space-y-1">
        {visibleItems.map((item) => {
          const isActive = currentPath === item.href;
          return (
            <li key={item.href}>
              <Link
                href={item.href}
                className={`block px-4 py-2 text-sm rounded-md mx-2 ${
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
  );
}
