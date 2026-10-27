package com.example.studentexpensetracker.ui.split;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;
import com.example.studentexpensetracker.databinding.ItemSharedExpenseBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SharedExpenseAdapter extends RecyclerView.Adapter<SharedExpenseAdapter.ViewHolder> {

    public interface OnDeleteExpenseClickListener {
        void onDeleteClick(SharedExpenseEntity expense);
    }

    private List<SharedExpenseEntity> expenses = new ArrayList<>();
    private final Map<Long, String> friendNames = new HashMap<>();
    private final OnDeleteExpenseClickListener deleteListener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public SharedExpenseAdapter(OnDeleteExpenseClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setMembers(List<FriendEntity> members) {
        friendNames.clear();
        if (members != null) {
            for (FriendEntity f : members) {
                friendNames.put(f.getFriendId(), f.getName());
            }
        }
        notifyDataSetChanged();
    }

    public void setExpenses(List<SharedExpenseEntity> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSharedExpenseBinding binding = ItemSharedExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSharedExpenseBinding binding;

        ViewHolder(ItemSharedExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SharedExpenseEntity item) {
            binding.tvExpenseDescription.setText(item.getDescription());

            String payerName = friendNames.getOrDefault(item.getPaidByFriendId(), "Friend #" + item.getPaidByFriendId());
            String dateStr = dateFormat.format(new Date(item.getDateTime()));
            binding.tvPaidByAndDate.setText("Paid by " + payerName + " • " + dateStr);

            binding.tvExpenseAmount.setText(currencyFormatter.format(item.getAmount()));

            binding.btnDeleteExpense.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(item);
                }
            });
        }
    }
}
