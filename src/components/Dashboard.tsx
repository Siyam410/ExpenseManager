// ...existing code...

export default function Dashboard({ transactions }: DashboardProps) {
  // ...existing code...

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-blue-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Balance</p>
              <p className="text-2xl font-bold text-gray-900">৳{totalBalance.toFixed(2)}</p>
            </div>
            <Wallet className="w-12 h-12 text-blue-500" />
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-green-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Income</p>
              <p className="text-2xl font-bold text-green-600">৳{totalIncome.toFixed(2)}</p>
            </div>
            <TrendingUp className="w-12 h-12 text-green-500" />
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-red-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Expenses</p>
              <p className="text-2xl font-bold text-red-600">৳{totalExpenses.toFixed(2)}</p>
            </div>
            <TrendingDown className="w-12 h-12 text-red-500" />
          </div>
        </div>
      </div>

      {/* ...existing code... */}
    </div>
  );
}

