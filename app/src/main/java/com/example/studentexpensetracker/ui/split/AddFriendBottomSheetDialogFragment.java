package com.example.studentexpensetracker.ui.split;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.databinding.BottomSheetAddFriendBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddFriendBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetAddFriendBinding binding;
    private GroupDetailViewModel viewModel;

    public static AddFriendBottomSheetDialogFragment newInstance() {
        return new AddFriendBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddFriendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GroupDetailViewModel.class);

        binding.btnAddFriend.setOnClickListener(v -> {
            String name = binding.etFriendName.getText() != null ? binding.etFriendName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }
            String phone = binding.etFriendPhone.getText() != null ? binding.etFriendPhone.getText().toString().trim() : "";
            viewModel.addFriendToGroup(name, phone);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
