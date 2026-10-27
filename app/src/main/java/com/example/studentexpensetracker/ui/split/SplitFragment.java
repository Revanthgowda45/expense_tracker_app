package com.example.studentexpensetracker.ui.split;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studentexpensetracker.databinding.FragmentSplitBinding;

public class SplitFragment extends Fragment {

    private FragmentSplitBinding binding;
    private SplitViewModel viewModel;
    private GroupAdapter adapter;

    public static SplitFragment newInstance() {
        return new SplitFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SplitViewModel.class);

        setupRecyclerView();

        binding.btnCreateGroup.setOnClickListener(v -> {
            CreateGroupBottomSheetDialogFragment.newInstance(groupId -> {
                Intent intent = new Intent(requireContext(), GroupDetailActivity.class);
                intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, groupId);
                startActivity(intent);
            }).show(getParentFragmentManager(), "CreateGroup");
        });

        viewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups == null || groups.isEmpty()) {
                binding.rvGroups.setVisibility(View.GONE);
                binding.tvNoGroups.setVisibility(View.VISIBLE);
            } else {
                binding.rvGroups.setVisibility(View.VISIBLE);
                binding.tvNoGroups.setVisibility(View.GONE);
                adapter.submitList(groups);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter(group -> {
            Intent intent = new Intent(requireContext(), GroupDetailActivity.class);
            intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getGroupId());
            startActivity(intent);
        });

        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
