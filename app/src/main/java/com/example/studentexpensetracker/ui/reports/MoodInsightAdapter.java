package com.example.studentexpensetracker.ui.reports;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.databinding.ItemMoodInsightBinding;
import com.example.studentexpensetracker.ui.reports.mood.MoodInsight;

import java.util.ArrayList;
import java.util.List;

public class MoodInsightAdapter extends RecyclerView.Adapter<MoodInsightAdapter.ViewHolder> {

    private List<MoodInsight> insights = new ArrayList<>();

    public void setInsights(List<MoodInsight> insights) {
        this.insights = insights != null ? insights : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMoodInsightBinding binding = ItemMoodInsightBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(insights.get(position));
    }

    @Override
    public int getItemCount() {
        return insights.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMoodInsightBinding binding;

        ViewHolder(ItemMoodInsightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MoodInsight insight) {
            binding.tvInsightEmoji.setText(insight.getEmoji());
            binding.tvInsightTitle.setText(insight.getTitle());
            binding.tvInsightDescription.setText(insight.getDescription());
        }
    }
}
