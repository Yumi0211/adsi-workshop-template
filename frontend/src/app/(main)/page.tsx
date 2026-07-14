import { Card } from "@/components/ui/Card";

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">ダッシュボード</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card title="今日の状態">
          <p className="text-sm text-gray-600">打刻機能は Unit 09 で実装されます</p>
        </Card>
        <Card title="今月のサマリー">
          <p className="text-sm text-gray-600">勤怠集計は Unit 03 で実装されます</p>
        </Card>
        <Card title="通知">
          <p className="text-sm text-gray-600">アラートは Unit 06 で実装されます</p>
        </Card>
      </div>
    </div>
  );
}
