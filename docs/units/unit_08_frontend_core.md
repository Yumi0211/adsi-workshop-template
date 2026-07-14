# Unit 08: Frontend 基盤

## 概要

Next.js プロジェクトの骨格。レイアウト・認証・API Client・共通コンポーネント。

## スコープ

- Next.js プロジェクト生成（App Router）
- Tailwind CSS 設定
- レスポンシブレイアウト（ヘッダー + サイドナビ + メインコンテンツ）
- Microsoft Entra ID SSO 連携（NextAuth.js or MSAL）
- API Client（fetch ラッパー、withBasePath 対応）
- 共通 UI コンポーネント（Button, Input, Table, Card, Modal, Alert）
- ロールベースのナビゲーション表示制御
- エラーハンドリング（エラーバウンダリ、トースト通知）
- ローディング状態の共通処理

## 依存

- unit_00_foundation（Backend API の基本動作確認）

## 完了条件

- [ ] `npm run dev` で起動する
- [ ] SSO ログイン → ユーザー情報表示ができる
- [ ] API Client が Backend と通信できる
- [ ] ロール別にナビゲーションが切り替わる
- [ ] モバイル/デスクトップでレイアウトが切り替わる
- [ ] SageMaker プレビュー（basePath）対応
