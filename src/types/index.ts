export interface Transaction {
  id: string;
  amount: number;
  category: string | null; // Allow null for income transactions
  paymentMethod: string;
  type: 'income' | 'expense';
  date: string;
  description: string;
  title?: string; // Add optional title field
}

// ...existing code...

