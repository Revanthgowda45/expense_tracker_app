package com.example.studentexpensetracker.ui.reports;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.databinding.ItemCategoryReportBinding;
import com.example.studentexpensetracker.ui.components.CategoryUiHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {

    public static class CategoryItem {
        private final String name;
        private final double amount;
        private final float percentage;

        public CategoryItem(String name, double amount, float percentage) {
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        public float getPercentage() {
            return percentage;
        }
    }

    private List<CategoryItem> items = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public void setItems(List<CategoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryReportBinding binding = ItemCategoryReportBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryReportBinding binding;

        ViewHolder(ItemCategoryReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryItem item) {
            binding.tvCategoryName.setText(item.getName());
            binding.tvCategoryAmount.setText(currencyFormatter.format(item.getAmount()));
            binding.tvCategoryPercentage.setText(String.format(Locale.US, "%.1f%% of total spending", item.getPercentage()));

            int progress = Math.min(100, Math.max(0, Math.round(item.getPercentage())));
            binding.pbCategoryProgress.setProgress(progress);

            CategoryUiHelper.CategoryVisual visual = CategoryUiHelper.getVisual(item.getName(), false);
            binding.ivCategoryIcon.setImageResource(visual.getIconResId());
            binding.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(visual.getColor()));
            binding.pbCategoryProgress.setIndicatorColor(visual.getColor());
        }
    }
}
