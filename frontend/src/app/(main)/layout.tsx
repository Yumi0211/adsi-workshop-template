"use client";

import { useState, useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { Header } from "@/components/layout/Header";
import { SideNav } from "@/components/layout/SideNav";
import { MobileNav } from "@/components/layout/MobileNav";
import { Loading } from "@/components/ui/Loading";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  const { user, isLoading, isAuthenticated, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace("/login");
    }
  }, [isLoading, isAuthenticated, router]);

  if (isLoading || !isAuthenticated) {
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
