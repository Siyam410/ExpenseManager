// ...existing code...

export default function TransactionList({ transactions, onDeleteTransaction }: TransactionListProps) {
  // ...existing code...

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* ...existing code... */}

      <div className="space-y-3">
        {filteredTransactions.map((transaction) => (
          <div
            key={transaction.id}
            className="bg-white p-4 rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div
                  className={`w-12 h-12 rounded-full flex items-center justify-center ${
                    transaction.type === 'income'
                      ? 'bg-green-100'
                      : 'bg-red-100'
                  }`}
                >
                  {transaction.type === 'income' ? (
                    <TrendingUp className="w-6 h-6 text-green-600" />
                  ) : (
                    <TrendingDown className="w-6 h-6 text-red-600" />
                  )}
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">
                    {/* Display "Income" for income, category name for expense */}
                    {transaction.title || (transaction.type === 'income' ? 'Income' : transaction.category)}
                  </h3>
                  <p className="text-sm text-gray-500">
                    {transaction.paymentMethod} • {transaction.date}
                  </p>
                  {transaction.description && (
                    <p className="text-sm text-gray-600 mt-1">
                      {transaction.description}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <span
                  className={`text-lg font-bold ${
                    transaction.type === 'income'
                      ? 'text-green-600'
                      : 'text-red-600'
                  }`}
                >
                  {/* Display amount with ৳ symbol */}
                  {transaction.type === 'income' ? '+' : '-'}৳{transaction.amount.toFixed(2)}
                </span>
                <button
                  onClick={() => onDeleteTransaction(transaction.id)}
                  className="text-gray-400 hover:text-red-600 transition-colors"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* ...existing code... */}
    </div>
  );
}

