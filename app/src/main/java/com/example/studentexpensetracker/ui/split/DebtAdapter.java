package com.example.studentexpensetracker.ui.split;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.databinding.ItemDebtBinding;
import com.example.studentexpensetracker.domain.split.Debt;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.ViewHolder> {

    public interface OnSettleClickListener {
        void onSettleClick(Debt debt, String fromName, String toName);
    }

    private List<Debt> debts = new ArrayList<>();
    private final Map<Long, String> friendNames = new HashMap<>();
    private final OnSettleClickListener settleClickListener;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DebtAdapter(OnSettleClickListener settleClickListener) {
        this.settleClickListener = settleClickListener;
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

    public void setDebts(List<Debt> debts) {
        this.debts = debts != null ? debts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDebtBinding binding = ItemDebtBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(debts.get(position));
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDebtBinding binding;

        ViewHolder(ItemDebtBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Debt debt) {
            String fromName = friendNames.getOrDefault(debt.getFromId(), "Friend #" + debt.getFromId());
            String toName = friendNames.getOrDefault(debt.getToId(), "Friend #" + debt.getToId());

            binding.tvDebtDescription.setText(fromName + " owes " + toName);
            binding.tvDebtAmount.setText(currencyFormatter.format(debt.getAmount()));

            binding.btnSettleDebt.setOnClickListener(v -> {
                if (settleClickListener != null) {
                    settleClickListener.onSettleClick(debt, fromName, toName);
                }
            });
        }
    }
}
