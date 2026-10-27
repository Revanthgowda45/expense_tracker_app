package com.example.studentexpensetracker.ui.split;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.databinding.BottomSheetAddSharedExpenseBinding;
import com.example.studentexpensetracker.domain.split.SplitCalculator;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AddSharedExpenseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetAddSharedExpenseBinding binding;
    private GroupDetailViewModel viewModel;
    private List<FriendEntity> members = new ArrayList<>();

    public static AddSharedExpenseBottomSheetDialogFragment newInstance() {
        return new AddSharedExpenseBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddSharedExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GroupDetailViewModel.class);

        viewModel.getGroupMembers().observe(getViewLifecycleOwner(), memberList -> {
            if (memberList != null) {
                members = memberList;
                List<String> names = new ArrayList<>();
                for (FriendEntity f : members) {
                    names.add(f.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                binding.spinnerPaidBy.setAdapter(adapter);
            }
        });

        binding.btnSubmitExpense.setOnClickListener(v -> {
            String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
            String amountStr = binding.etAmount.getText() != null ? binding.etAmount.getText().toString().trim() : "";

            if (desc.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an expense description", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalAmount;
            try {
                totalAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (totalAmount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPos = binding.spinnerPaidBy.getSelectedItemPosition();
            if (selectedPos < 0 || selectedPos >= members.size()) {
                Toast.makeText(requireContext(), "Please select who paid", Toast.LENGTH_SHORT).show();
                return;
            }

            long paidByFriendId = members.get(selectedPos).getFriendId();

            List<Long> memberIds = new ArrayList<>();
            for (FriendEntity f : members) {
                memberIds.add(f.getFriendId());
            }

            List<ExpenseShareEntity> shares = SplitCalculator.splitEqually(totalAmount, memberIds);
            boolean addToPersonal = binding.cbAddToPersonal.isChecked();

            viewModel.addSharedExpense(desc, totalAmount, paidByFriendId, shares, addToPersonal);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
