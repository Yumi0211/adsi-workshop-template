"use client";

interface HeaderProps {
  userName: string;
  onLogout: () => void;
  onMenuToggle: () => void;
}

export function Header({ userName, onLogout, onMenuToggle }: HeaderProps) {
  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 lg:px-6">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuToggle}
          className="lg:hidden p-2 rounded-md hover:bg-gray-100"
          aria-label="メニュー"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <h1 className="text-lg font-semibold text-gray-900">勤怠管理システム</h1>
      </div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-700">{userName}</span>
        <button
          onClick={onLogout}
          className="text-sm text-gray-500 hover:text-gray-700"
        >
          ログアウト
        </button>
      </div>
    </header>
  );
}
