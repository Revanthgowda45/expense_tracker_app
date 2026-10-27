package com.example.studentexpensetracker.ui.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studentexpensetracker.R;
import com.example.studentexpensetracker.databinding.FragmentTransactionsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private TransactionsViewModel viewModel;
    private TransactionAdapter adapter;

    public static TransactionsFragment newInstance() {
        return new TransactionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        setupRecyclerView();
        setupFiltersAndSearch();

        viewModel.getFilteredTransactions().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.rvTransactions.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvTransactions.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setVisibility(View.GONE);
                adapter.submitList(list);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(
                transaction -> {
                    Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                    intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transaction.getId());
                    startActivity(intent);
                },
                transaction -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Transaction")
                            .setMessage("Are you sure you want to delete this record?")
                            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTransaction(transaction))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupFiltersAndSearch() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipExpense)) {
                viewModel.setFilterType("EXPENSE");
            } else if (checkedIds.contains(R.id.chipIncome)) {
                viewModel.setFilterType("INCOME");
            } else {
                viewModel.setFilterType("ALL");
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
