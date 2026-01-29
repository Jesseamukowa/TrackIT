package com.example.budgettracker;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {
    private Cursor cursor;

    public CategoryReportAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_stats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String name = cursor.getString(0);
            double total = cursor.getDouble(1);
            int count = cursor.getInt(2);

            holder.tvName.setText(name);
            holder.tvCount.setText(count + (count == 1 ? " transaction" : " transactions"));
            holder.tvTotal.setText("KES " + String.format("%.2f", total));

            // Set icon color based on category
            int color = getCategoryColor(name);
            holder.iconContainer.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (cursor.moveToPosition(position)) {
            String name = cursor.getString(0);

            // Use our helper to get the right color
            int color = StatsActivity.getCategoryColor(name);

            // Apply the color to the circular background (iconContainer)
            holder.iconContainer.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

            holder.tvName.setText(name);
            // ... rest of your binding code
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    // Helper to match colors to your UI design
    private int getCategoryColor(String name) {
        switch (name) {
            case "Food": return Color.parseColor("#FFA500");
            case "Health": return Color.parseColor("#FF4D4D");
            case "Transport": return Color.parseColor("#4285F4");
            default: return Color.parseColor("#9C27B0");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount, tvTotal;
        View iconContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvTransactionCount);
            tvTotal = itemView.findViewById(R.id.tvCategoryTotal);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }

    // Inside CategoryReportAdapter.java
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close(); // Close the old one to save memory
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); // Tell the list to redraw itself
        }
    }
}