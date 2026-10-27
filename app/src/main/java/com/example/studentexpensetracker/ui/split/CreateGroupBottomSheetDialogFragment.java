package com.example.studentexpensetracker.ui.split;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.databinding.BottomSheetCreateGroupBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnGroupCreatedListener {
        void onGroupCreated(long groupId);
    }

    private BottomSheetCreateGroupBinding binding;
    private SplitViewModel viewModel;
    private OnGroupCreatedListener listener;

    public static CreateGroupBottomSheetDialogFragment newInstance(OnGroupCreatedListener listener) {
        CreateGroupBottomSheetDialogFragment fragment = new CreateGroupBottomSheetDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCreateGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SplitViewModel.class);

        binding.btnSubmitGroup.setOnClickListener(v -> {
            String name = binding.etGroupName.getText() != null ? binding.etGroupName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            String desc = binding.etGroupDesc.getText() != null ? binding.etGroupDesc.getText().toString().trim() : "";
            String friendsStr = binding.etGroupFriends.getText() != null ? binding.etGroupFriends.getText().toString().trim() : "";

            List<FriendEntity> friends = new ArrayList<>();
            // Always include "You"
            friends.add(new FriendEntity("You", null));

            if (!friendsStr.isEmpty()) {
                String[] parts = friendsStr.split(",");
                for (String p : parts) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty() && !"You".equalsIgnoreCase(trimmed)) {
                        friends.add(new FriendEntity(trimmed, null));
                    }
                }
            }

            viewModel.createGroup(name, desc, friends, groupId -> {
                if (listener != null) {
                    listener.onGroupCreated(groupId);
                }
                dismiss();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
