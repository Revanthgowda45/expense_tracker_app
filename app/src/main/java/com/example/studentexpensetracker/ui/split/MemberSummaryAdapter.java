package com.example.studentexpensetracker.ui.split;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.databinding.ItemMemberSummaryBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MemberSummaryAdapter extends RecyclerView.Adapter<MemberSummaryAdapter.ViewHolder> {

    private List<MemberBalanceSummary> summaries = new ArrayList<>();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public void setSummaries(List<MemberBalanceSummary> list) {
        this.summaries = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberSummaryBinding binding = ItemMemberSummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(summaries.get(position));
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberSummaryBinding binding;

        ViewHolder(ItemMemberSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemberBalanceSummary item) {
            String name = item.getFriend().getName();
            binding.tvMemberName.setText(name);

            String initial = !name.isEmpty() ? name.substring(0, 1).toUpperCase(Locale.ROOT) : "?";
            binding.tvMemberAvatar.setText(initial);

            binding.tvMemberPaidVsShare.setText("Paid " + currencyFormatter.format(item.getTotalPaid()) + " • Share " + currencyFormatter.format(item.getTotalShare()));

            double net = item.getNetBalance();
            if (Math.abs(net) < 0.01) {
                binding.tvMemberNetBalance.setText("Settled");
                binding.tvMemberNetBalance.setTextColor(0xFF94A3B8); // Slate
            } else if (net > 0) {
                binding.tvMemberNetBalance.setText("+" + currencyFormatter.format(net));
                binding.tvMemberNetBalance.setTextColor(0xFF10B981); // Emerald
            } else {
                binding.tvMemberNetBalance.setText(currencyFormatter.format(net));
                binding.tvMemberNetBalance.setTextColor(0xFFEF4444); // Red
            }
        }
    }
}
