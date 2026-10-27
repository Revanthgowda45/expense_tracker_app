package com.example.studentexpensetracker.ui.transactions;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.databinding.ItemTransactionBinding;
import com.example.studentexpensetracker.ui.components.CategoryUiHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionEntity transaction);
    }

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(TransactionEntity transaction);
    }

    private final OnTransactionClickListener clickListener;
    private final OnTransactionLongClickListener longClickListener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public TransactionAdapter(OnTransactionClickListener clickListener, OnTransactionLongClickListener longClickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public TransactionAdapter(OnTransactionClickListener clickListener) {
        this(clickListener, null);
    }

    private static final DiffUtil.ItemCallback<TransactionEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getDateTime() == newItem.getDateTime() &&
                    safeEquals(oldItem.getNote(), newItem.getNote()) &&
                    safeEquals(oldItem.getType(), newItem.getType()) &&
                    safeEquals(oldItem.getMood(), newItem.getMood()) &&
                    safeEquals(oldItem.getReceiptImagePath(), newItem.getReceiptImagePath());
        }

        private boolean safeEquals(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransactionEntity item) {
            boolean isIncome = "INCOME".equalsIgnoreCase(item.getType());

            CategoryUiHelper.CategoryVisual visual = CategoryUiHelper.getVisual(item.getNote(), isIncome);
            binding.ivCategoryIcon.setImageResource(visual.getIconResId());
            binding.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(visual.getColor()));
            binding.iconContainer.setBackgroundTintList(ColorStateList.valueOf(androidx.core.graphics.ColorUtils.setAlphaComponent(visual.getColor(), 35)));

            String note = item.getNote();
            if (note == null || note.trim().isEmpty()) {
                note = isIncome ? "Income" : "Expense";
            }
            binding.tvTitle.setText(note);

            binding.tvDate.setText(dateFormat.format(new Date(item.getDateTime())));

            String paymentMethod = item.getPaymentMethod();
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                binding.tvPaymentMethod.setVisibility(View.VISIBLE);
                binding.tvPaymentMethod.setText("• " + paymentMethod);
            } else {
                binding.tvPaymentMethod.setVisibility(View.GONE);
            }

            String mood = item.getMood();
            if (mood != null && !mood.trim().isEmpty()) {
                binding.tvMoodEmoji.setVisibility(View.VISIBLE);
                binding.tvMoodEmoji.setText(com.example.studentexpensetracker.ui.reports.mood.MoodInsightsGenerator.getMoodEmoji(mood));
            } else {
                binding.tvMoodEmoji.setVisibility(View.GONE);
            }

            if (item.getReceiptImagePath() != null && !item.getReceiptImagePath().trim().isEmpty()) {
                binding.ivReceiptBadge.setVisibility(View.VISIBLE);
            } else {
                binding.ivReceiptBadge.setVisibility(View.GONE);
            }

            String formattedAmount = currencyFormatter.format(item.getAmount());
            if (isIncome) {
                binding.tvAmount.setText("+" + formattedAmount);
                binding.tvAmount.setTextColor(0xFF10B981); // Emerald
            } else {
                binding.tvAmount.setText("-" + formattedAmount);
                binding.tvAmount.setTextColor(0xFFEF4444); // Red
            }

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTransactionClick(item);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onTransactionLongClick(item);
                    return true;
                }
                return false;
            });
        }
    }
}
