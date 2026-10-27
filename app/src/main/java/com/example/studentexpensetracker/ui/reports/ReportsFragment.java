package com.example.studentexpensetracker.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studentexpensetracker.R;
import com.example.studentexpensetracker.databinding.FragmentReportsBinding;
import com.example.studentexpensetracker.ui.reports.mood.MoodAnalytics;
import com.example.studentexpensetracker.ui.reports.mood.MoodStat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsViewModel viewModel;
    private CategoryReportAdapter categoryAdapter;
    private MoodInsightAdapter moodInsightAdapter;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private double currentIncome = 0.0;
    private double currentExpense = 0.0;

    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        setupRecyclerViews();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryReportAdapter();
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(categoryAdapter);

        moodInsightAdapter = new MoodInsightAdapter();
        binding.rvMoodInsights.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMoodInsights.setAdapter(moodInsightAdapter);
    }

    private void observeViewModel() {
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            currentIncome = income != null ? income : 0.0;
            binding.tvTotalInflow.setText(currencyFormatter.format(currentIncome));
            updateNetSavings();
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            currentExpense = expense != null ? expense : 0.0;
            binding.tvTotalOutflow.setText(currencyFormatter.format(currentExpense));
            updateNetSavings();
        });

        viewModel.getExpensesByCategory().observe(getViewLifecycleOwner(), catMap -> {
            if (catMap == null || catMap.isEmpty()) {
                binding.rvCategories.setVisibility(View.GONE);
                binding.tvNoCategories.setVisibility(View.VISIBLE);
                return;
            }

            double total = 0.0;
            for (Double amt : catMap.values()) {
                if (amt != null) total += amt;
            }

            if (total <= 0.0) {
                binding.rvCategories.setVisibility(View.GONE);
                binding.tvNoCategories.setVisibility(View.VISIBLE);
                return;
            }

            binding.rvCategories.setVisibility(View.VISIBLE);
            binding.tvNoCategories.setVisibility(View.GONE);

            List<Map.Entry<String, Double>> sortedList = new ArrayList<>(catMap.entrySet());
            sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            List<CategoryReportAdapter.CategoryItem> items = new ArrayList<>();
            for (Map.Entry<String, Double> entry : sortedList) {
                double amt = entry.getValue();
                float pct = (float) ((amt / total) * 100.0);
                items.add(new CategoryReportAdapter.CategoryItem(entry.getKey(), amt, pct));
            }
            categoryAdapter.setItems(items);
        });

        viewModel.getMoodAnalytics().observe(getViewLifecycleOwner(), analytics -> {
            if (analytics == null) return;

            if (analytics.isThresholdReached()) {
                binding.cardThresholdNotice.setVisibility(View.GONE);
            } else {
                binding.cardThresholdNotice.setVisibility(View.VISIBLE);
                int remaining = analytics.getRemainingToThreshold();
                binding.tvThresholdMessage.setText(
                        "Log " + remaining + " more transaction" + (remaining == 1 ? "" : "s") +
                        " with mood tags to reveal deep emotional spending triggers."
                );
            }

            MoodStat highestSpend = analytics.getHighestSpendMood();
            MoodStat highestAvg = analytics.getHighestAvgMood();

            if (highestSpend != null || highestAvg != null) {
                binding.cardDominantMoods.setVisibility(View.VISIBLE);
                if (highestSpend != null) {
                    binding.tvHighestSpendMood.setText(
                            highestSpend.getEmoji() + " Highest Spend Mood: " +
                            highestSpend.getMood() + " (" + currencyFormatter.format(highestSpend.getTotalAmount()) + ")"
                    );
                } else {
                    binding.tvHighestSpendMood.setText("");
                }

                if (highestAvg != null) {
                    binding.tvHighestAvgMood.setText(
                            highestAvg.getEmoji() + " Highest Avg Per Spend: " +
                            highestAvg.getMood() + " (" + currencyFormatter.format(highestAvg.getAvgAmount()) + "/tx)"
                    );
                } else {
                    binding.tvHighestAvgMood.setText("");
                }
            } else {
                binding.cardDominantMoods.setVisibility(View.GONE);
            }
        });

        viewModel.getMoodInsights().observe(getViewLifecycleOwner(), insights -> {
            boolean empty = (insights == null || insights.isEmpty());
            binding.tvNoMoodInsights.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvMoodInsights.setVisibility(empty ? View.GONE : View.VISIBLE);
            moodInsightAdapter.setInsights(insights);
        });
    }

    private void updateNetSavings() {
        double net = currentIncome - currentExpense;
        binding.tvNetSavings.setText(currencyFormatter.format(net));
        if (net >= 0) {
            binding.tvNetSavings.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
        } else {
            binding.tvNetSavings.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
