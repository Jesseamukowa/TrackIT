package com.example.budgettracker;

import android.view.LayoutInflater;
import com.example.budgettracker.R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private final String[] titles = {"Smart Budgeting", "Powerful Insights", "Full Control"};
    private final String[] descriptions = {
            "Track every cent easily. Categorize your income and expenses automatically.",
            "Visualize your financial growth with beautiful charts and deep analytics.",
            "Set limits, save more, and manage your financial future with total security."
    };
    private final int[] images = {
            android.R.drawable.ic_menu_edit,
            R.drawable.ic_insights,
            android.R.drawable.ic_lock_idle_lock
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(titles[position]);
        holder.tvDesc.setText(descriptions[position]);
        holder.ivImg.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() { return titles.length; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        ImageView ivImg;

        ViewHolder(View itemView) {
            super(itemView);
            // These MUST match the IDs in the XML you provided
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            ivImg = itemView.findViewById(R.id.ivIllustration);
        }
    }
}