package com.example.studentexpensetracker.ui.split;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.databinding.ItemGroupBinding;

public class GroupAdapter extends ListAdapter<ExpenseGroupEntity, GroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(ExpenseGroupEntity group);
    }

    private final OnGroupClickListener clickListener;

    public GroupAdapter(OnGroupClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<ExpenseGroupEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExpenseGroupEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExpenseGroupEntity oldItem, @NonNull ExpenseGroupEntity newItem) {
            return oldItem.getGroupId() == newItem.getGroupId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExpenseGroupEntity oldItem, @NonNull ExpenseGroupEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    (oldItem.getDescription() == null ? newItem.getDescription() == null : oldItem.getDescription().equals(newItem.getDescription()));
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
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
        private final ItemGroupBinding binding;

        ViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ExpenseGroupEntity item) {
            binding.tvGroupName.setText(item.getName());
            String desc = item.getDescription();
            binding.tvGroupDesc.setText(desc != null && !desc.trim().isEmpty() ? desc : "Group expenses & bill splits");

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onGroupClick(item);
                }
            });
        }
    }
}
