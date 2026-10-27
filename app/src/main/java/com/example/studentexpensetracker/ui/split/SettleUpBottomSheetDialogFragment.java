package com.example.studentexpensetracker.ui.split;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.databinding.BottomSheetSettleUpBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettleUpBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_FROM_ID = "from_id";
    private static final String ARG_TO_ID = "to_id";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_FROM_NAME = "from_name";
    private static final String ARG_TO_NAME = "to_name";

    private BottomSheetSettleUpBinding binding;
    private GroupDetailViewModel viewModel;

    private long fromId;
    private long toId;
    private double defaultAmount;
    private String fromName;
    private String toName;

    public static SettleUpBottomSheetDialogFragment newInstance(long fromId, long toId, double amount, String fromName, String toName) {
        SettleUpBottomSheetDialogFragment fragment = new SettleUpBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FROM_ID, fromId);
        args.putLong(ARG_TO_ID, toId);
        args.putDouble(ARG_AMOUNT, amount);
        args.putString(ARG_FROM_NAME, fromName);
        args.putString(ARG_TO_NAME, toName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetSettleUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GroupDetailViewModel.class);

        if (getArguments() != null) {
            fromId = getArguments().getLong(ARG_FROM_ID);
            toId = getArguments().getLong(ARG_TO_ID);
            defaultAmount = getArguments().getDouble(ARG_AMOUNT);
            fromName = getArguments().getString(ARG_FROM_NAME, "Member");
            toName = getArguments().getString(ARG_TO_NAME, "Member");
        }

        binding.tvSettleDescription.setText(fromName + " pays " + toName);
        binding.etSettleAmount.setText(String.valueOf(defaultAmount));

        binding.btnConfirmSettle.setOnClickListener(v -> {
            String text = binding.etSettleAmount.getText() != null ? binding.etSettleAmount.getText().toString().trim() : "";
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter settlement amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(text);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.settleDebt(fromId, toId, amount);
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
