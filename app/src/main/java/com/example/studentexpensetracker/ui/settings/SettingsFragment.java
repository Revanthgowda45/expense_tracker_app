package com.example.studentexpensetracker.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.databinding.FragmentSettingsBinding;
import com.example.studentexpensetracker.ui.home.SetBudgetBottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private double currentMonthlyBudget = 0.0;
    private String currentUserName = "Student";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        observePreferences();
        setupClickListeners();
    }

    private void observePreferences() {
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            currentUserName = (name != null && !name.trim().isEmpty()) ? name : "Student";
            binding.tvCurrentUserName.setText(currentUserName);
        });

        viewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            currentMonthlyBudget = budget != null ? budget : 0.0;
            if (viewModel.isCustomBudget() && currentMonthlyBudget > 0) {
                binding.tvCurrentMonthlyBudget.setText("Custom Budget: " + currencyFormatter.format(currentMonthlyBudget));
            } else {
                binding.tvCurrentMonthlyBudget.setText("Dynamic (Automatically calculated from month's income)");
            }
        });

        viewModel.getAllowanceDay().observe(getViewLifecycleOwner(), day -> {
            int d = (day != null && day > 0) ? day : 1;
            binding.tvCurrentAllowanceDay.setText("Day " + d + " of every month");
        });

        viewModel.getSurvivalAnalysisDays().observe(getViewLifecycleOwner(), days -> {
            int count = (days != null && days > 0) ? days : 7;
            binding.tvCurrentSurvivalWindow.setText("Recent " + count + " Days Average");
        });

        viewModel.getDarkMode().observe(getViewLifecycleOwner(), isDark -> {
            boolean dark = (isDark != null && isDark);
            if (binding.switchDarkMode.isChecked() != dark) {
                binding.switchDarkMode.setChecked(dark);
            }
        });

        viewModel.getNotificationsEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            boolean enabled = (isEnabled != null && isEnabled);
            if (binding.switchNotifications.isChecked() != enabled) {
                binding.switchNotifications.setChecked(enabled);
            }
        });
    }

    private void setupClickListeners() {
        binding.rowUserName.setOnClickListener(v -> showEditUserNameDialog());

        binding.rowMonthlyBudget.setOnClickListener(v -> {
            SetBudgetBottomSheetDialogFragment dialog = SetBudgetBottomSheetDialogFragment.newInstance();
            dialog.show(getChildFragmentManager(), "set_budget");
        });

        binding.rowAllowanceDay.setOnClickListener(v -> showAllowanceDayPicker());

        binding.rowSurvivalWindow.setOnClickListener(v -> showSurvivalWindowPicker());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                viewModel.setDarkMode(isChecked);
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                viewModel.setNotificationsEnabled(isChecked);
            }
        });

        binding.rowClearData.setOnClickListener(v -> showClearDataConfirmationDialog());
    }

    private void showEditUserNameDialog() {
        final EditText input = new EditText(requireContext());
        input.setSingleLine(true);
        input.setText(currentUserName);
        input.setSelection(currentUserName.length());

        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 50;
        params.rightMargin = 50;
        params.topMargin = 20;
        params.bottomMargin = 10;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Name")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        viewModel.setUserName(newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAllowanceDayPicker() {
        final String[] days = new String[]{"1st", "5th", "10th", "15th", "20th", "25th", "28th"};
        final int[] values = new int[]{1, 5, 10, 15, 20, 25, 28};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Monthly Allowance Day")
                .setItems(days, (dialog, which) -> {
                    viewModel.setAllowanceDay(values[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSurvivalWindowPicker() {
        final String[] options = new String[]{"7 Days (Recommended)", "14 Days", "30 Days"};
        final int[] values = new int[]{7, 14, 30};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Burn Rate Lookback Window")
                .setItems(options, (dialog, which) -> {
                    viewModel.setSurvivalAnalysisDays(values[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearDataConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All Transactions?")
                .setMessage("This will permanently delete all your logged income and expenses. This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    viewModel.clearAllData(() -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "All transactions cleared", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
