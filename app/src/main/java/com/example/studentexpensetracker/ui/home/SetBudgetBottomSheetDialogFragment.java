package com.example.studentexpensetracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.databinding.BottomSheetSetBudgetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SetBudgetBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetSetBudgetBinding binding;
    private HomeViewModel viewModel;

    public static SetBudgetBottomSheetDialogFragment newInstance() {
        return new SetBudgetBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetSetBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        double currentBudget = viewModel.getEffectiveMonthlyBudget();
        if (currentBudget > 0) {
            binding.etBudgetAmount.setText(String.valueOf((long) currentBudget));
        }

        binding.btnSaveBudget.setOnClickListener(v -> {
            String text = binding.etBudgetAmount.getText() != null ? binding.etBudgetAmount.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                try {
                    double amount = Double.parseDouble(text);
                    if (amount > 0) {
                        viewModel.setMonthlyBudget(amount);
                        dismiss();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        binding.btnSyncIncome.setOnClickListener(v -> {
            Double income = viewModel.getTotalIncome().getValue();
            double syncAmt = (income != null && income > 0) ? income : 10000.0;
            viewModel.setMonthlyBudget(syncAmt);
            dismiss();
        });

        binding.btnResetBudget.setOnClickListener(v -> {
            viewModel.resetMonthlyBudget();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
