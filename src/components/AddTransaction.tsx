// ...existing code...

export default function AddTransaction() {
  const [type, setType] = useState<'income' | 'expense'>('expense');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [description, setDescription] = useState('');

  // ...existing code...

  const handleTypeChange = (newType: 'income' | 'expense') => {
    setType(newType);
    // Clear category when switching to income
    if (newType === 'income') {
      setCategory('');
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const transaction: Transaction = {
      id: Date.now().toString(),
      amount: parseFloat(amount),
      // Set category to null for income, selected category for expense
      category: type === 'income' ? null : category,
      paymentMethod,
      type,
      date,
      description,
      // Set title to "Income" for income, category name for expense
      title: type === 'income' ? 'Income' : category,
    };

    onAddTransaction(transaction);

    // Reset form
    setAmount('');
    setCategory('');
    setPaymentMethod('');
    setDescription('');
    setDate(new Date().toISOString().split('T')[0]);
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      {/* ...existing code... */}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Type Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Transaction Type
          </label>
          <div className="grid grid-cols-2 gap-4">
            <button
              type="button"
              onClick={() => handleTypeChange('expense')}
              className={`p-4 rounded-lg border-2 transition-all ${
                type === 'expense'
                  ? 'border-red-500 bg-red-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <TrendingDown className="w-6 h-6 mx-auto mb-2 text-red-500" />
              <span className="font-medium">Expense</span>
            </button>
            <button
              type="button"
              onClick={() => handleTypeChange('income')}
              className={`p-4 rounded-lg border-2 transition-all ${
                type === 'income'
                  ? 'border-green-500 bg-green-50'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <TrendingUp className="w-6 h-6 mx-auto mb-2 text-green-500" />
              <span className="font-medium">Income</span>
            </button>
          </div>
        </div>

        {/* Amount */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Amount (৳)
          </label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Enter amount"
            required
            step="0.01"
            min="0"
          />
        </div>

        {/* Category Selection - Hidden for Income */}
        {type === 'expense' && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Category
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            >
              <option value="">Select category</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Payment Method / Wallet */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {type === 'income' ? 'How did you receive?' : 'Payment Method'}
          </label>
          <div className="grid grid-cols-2 gap-3">
            {paymentMethods.map((method) => (
              <button
                key={method}
                type="button"
                onClick={() => setPaymentMethod(method)}
                className={`p-3 rounded-lg border-2 transition-all ${
                  paymentMethod === method
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                {method}
              </button>
            ))}
          </div>
        </div>

        {/* Date */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Date
          </label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            required
          />
        </div>

        {/* Description / Note */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Note (Optional)
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Add a note..."
            rows={3}
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium"
        >
          Add Transaction
        </button>
      </form>
    </div>
  );
}
