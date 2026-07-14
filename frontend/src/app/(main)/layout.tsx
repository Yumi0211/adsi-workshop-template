"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { Header } from "@/components/layout/Header";
import { SideNav } from "@/components/layout/SideNav";
import { MobileNav } from "@/components/layout/MobileNav";
import { Loading } from "@/components/ui/Loading";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  const { user, isLoading, logout } = useAuth();
  const pathname = usePathname();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loading />
      </div>
    );
  }

  const role = user?.role ?? "EMPLOYEE";
  const userName = user?.name ?? "ゲスト";

  return (
    <div className="min-h-screen flex flex-col">
      <Header
        userName={userName}
        onLogout={logout}
        onMenuToggle={() => setMobileMenuOpen(true)}
      />
      <div className="flex flex-1">
        <div className="hidden lg:block">
          <SideNav role={role} currentPath={pathname} />
        </div>
        <MobileNav
          role={role}
          currentPath={pathname}
          open={mobileMenuOpen}
          onClose={() => setMobileMenuOpen(false)}
        />
        <main className="flex-1 p-4 lg:p-6">{children}</main>
      </div>
    </div>
  );
}
