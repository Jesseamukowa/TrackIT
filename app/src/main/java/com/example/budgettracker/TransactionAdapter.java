package com.example.budgettracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(transaction.getDate());

        // Requirement: Format as -Amount in Red
        holder.tvAmount.setText("- " + String.format("%.2f", transaction.getAmount()));
        holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Catchy Red

        // CCO Touch: Dynamic Icon coloring for Critical vs Leisure
        if (isCritical(transaction.getCategory())) {
            holder.ivTypeIcon.setImageResource(R.drawable.ic_priority_high); // Use a '!' or circle icon
            holder.ivTypeIcon.setColorFilter(Color.parseColor("#FF9800")); // Orange for critical
        } else {
            holder.ivTypeIcon.setImageResource(R.drawable.ic_leisure); // Use a star or heart icon
            holder.ivTypeIcon.setColorFilter(Color.parseColor("#03DAC5")); // Teal for leisure
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // Update list data when returning from AddExpenseActivity
    public void updateData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    private boolean isCritical(String category) {
        // Logic to determine if an expense is essential
        return category.equalsIgnoreCase("Education") ||
                category.equalsIgnoreCase("Health") ||
                category.equalsIgnoreCase("Transport");
    }

    // Add this method inside your TransactionAdapter class
    public Transaction getTransactionAt(int position) {
        return transactionList.get(position);
    }



    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount;
        ImageView ivTypeIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
        }
    }
}