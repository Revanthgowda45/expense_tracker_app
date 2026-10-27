package com.example.studentexpensetracker.ui.home;

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
import com.example.studentexpensetracker.MainActivity;
import com.example.studentexpensetracker.databinding.FragmentHomeBinding;
import com.example.studentexpensetracker.domain.SurvivalStats;
import com.example.studentexpensetracker.domain.SurvivalStatus;
import com.example.studentexpensetracker.ui.reports.mood.WeeklyMoodSummary;
import com.example.studentexpensetracker.ui.transactions.AddEditTransactionActivity;
import com.example.studentexpensetracker.ui.transactions.TransactionAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.btnEditBudget.setOnClickListener(v -> {
            SetBudgetBottomSheetDialogFragment.newInstance().show(getParentFragmentManager(), "SetBudget");
        });

        binding.cardBudget.setOnClickListener(v -> {
            SetBudgetBottomSheetDialogFragment.newInstance().show(getParentFragmentManager(), "SetBudget");
        });

        binding.btnViewMoodReports.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(2); // Reports tab
            }
        });
        binding.btnSeeAllTransactions.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(1); // Records tab
            }
        });

        binding.ivHeaderShield.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(2); // Reports tab
            }
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transaction -> {
            Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
            intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transaction.getId());
            startActivity(intent);
        });

        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupObservers() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        binding.tvCurrentDate.setText(dateFormat.format(new Date()));

        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            String greeting = "Hey, " + (name != null && !name.trim().isEmpty() ? name : "Student") + "! 👋";
            binding.tvGreeting.setText(greeting);
        });

        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), balance -> {
            double bal = balance != null ? balance : 0.0;
            binding.tvTotalBalance.setText(currencyFormat.format(bal));
        });

        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            double inc = income != null ? income : 0.0;
            binding.tvTotalIncome.setText(currencyFormat.format(inc));
            updateBudgetProgress();
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            double exp = expense != null ? expense : 0.0;
            binding.tvTotalExpense.setText(currencyFormat.format(exp));
            updateBudgetProgress();
        });

        viewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            updateBudgetProgress();
        });

        viewModel.getSurvivalStats().observe(getViewLifecycleOwner(), this::bindSurvivalStats);

        viewModel.getWeeklyMoodSummary().observe(getViewLifecycleOwner(), this::bindWeeklyMood);

        viewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                binding.rvRecentTransactions.setVisibility(View.GONE);
                binding.tvNoTransactions.setVisibility(View.VISIBLE);
            } else {
                binding.rvRecentTransactions.setVisibility(View.VISIBLE);
                binding.tvNoTransactions.setVisibility(View.GONE);
                transactionAdapter.submitList(transactions);
            }
        });
    }

    private void bindSurvivalStats(SurvivalStats stats) {
        if (stats == null) return;

        binding.tvRunwayDays.setText(stats.getDaysLeft() + " Days Left");

        if (stats.getStatus() == SurvivalStatus.SAFE) {
            binding.tvSurvivalStatusBadge.setText("● SAFE");
            binding.tvSurvivalStatusBadge.setTextColor(0xFF10B981);
            binding.tvSurvivalStatusBadge.setBackgroundResource(com.example.studentexpensetracker.R.drawable.bg_badge_safe);
        } else if (stats.getStatus() == SurvivalStatus.TIGHT) {
            binding.tvSurvivalStatusBadge.setText("▲ TIGHT");
            binding.tvSurvivalStatusBadge.setTextColor(0xFFF59E0B);
            binding.tvSurvivalStatusBadge.setBackgroundResource(com.example.studentexpensetracker.R.drawable.bg_badge_tight);
        } else {
            binding.tvSurvivalStatusBadge.setText("✕ DANGER");
            binding.tvSurvivalStatusBadge.setTextColor(0xFFEF4444);
            binding.tvSurvivalStatusBadge.setBackgroundResource(com.example.studentexpensetracker.R.drawable.bg_badge_danger);
        }

        String sub = "Allowance in " + stats.getDaysUntilNextIncome() + " days • Burn rate: " + currencyFormat.format(stats.getAvgDailySpend()) + "/day";
        binding.tvRunwaySubtitle.setText(sub);

        binding.tvSafeDailyLimit.setText(currencyFormat.format(stats.getSafeDailyLimit()) + "/day");
        binding.tvTodayRemaining.setText(currencyFormat.format(stats.getTodayRemaining()));
    }

    private void updateBudgetProgress() {
        Double expObj = viewModel.getTotalExpense().getValue();
        double spent = expObj != null ? expObj : 0.0;
        double budget = viewModel.getEffectiveMonthlyBudget();

        binding.tvBudgetSpentVsTotal.setText(currencyFormat.format(spent) + " / " + currencyFormat.format(budget));

        double remaining = Math.max(0.0, budget - spent);
        int percent = (int) Math.min(100, (spent / budget) * 100);
        binding.tvBudgetRemaining.setText(currencyFormat.format(remaining) + " left (" + percent + "% spent)");

        binding.progressBudget.setProgress(percent);
        if (percent < 75) {
            binding.progressBudget.setIndicatorColor(0xFF10B981);
        } else if (percent < 90) {
            binding.progressBudget.setIndicatorColor(0xFFF59E0B);
        } else {
            binding.progressBudget.setIndicatorColor(0xFFEF4444);
        }
    }

    private void bindWeeklyMood(WeeklyMoodSummary summary) {
        if (summary == null) {
            binding.cardWeeklyMood.setVisibility(View.GONE);
            return;
        }

        binding.cardWeeklyMood.setVisibility(View.VISIBLE);
        binding.tvMoodHeadline.setText(summary.getHeadline());
        binding.tvMoodSummaryInsight.setText(summary.getSummaryInsight());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
