package com.example.studentexpensetracker.ui.split;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studentexpensetracker.databinding.ActivityGroupDetailBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class GroupDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    private ActivityGroupDetailBinding binding;
    private GroupDetailViewModel viewModel;
    private DebtAdapter debtAdapter;
    private MemberSummaryAdapter memberSummaryAdapter;
    private SharedExpenseAdapter sharedExpenseAdapter;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        if (groupId <= 0) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.init(groupId);

        setupToolbar();
        setupRecyclerViews();
        observeViewModel();
        setupActions();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Debts
        debtAdapter = new DebtAdapter((debt, fromName, toName) -> {
            SettleUpBottomSheetDialogFragment dialog = SettleUpBottomSheetDialogFragment.newInstance(
                    debt.getFromId(),
                    debt.getToId(),
                    debt.getAmount(),
                    fromName,
                    toName
            );
            dialog.show(getSupportFragmentManager(), "settle_up");
        });
        binding.rvDebts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDebts.setAdapter(debtAdapter);

        // Member summaries
        memberSummaryAdapter = new MemberSummaryAdapter();
        binding.rvMemberSummaries.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMemberSummaries.setAdapter(memberSummaryAdapter);

        // Group expenses
        sharedExpenseAdapter = new SharedExpenseAdapter(expense -> viewModel.deleteSharedExpense(expense));
        binding.rvGroupExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroupExpenses.setAdapter(sharedExpenseAdapter);
    }

    private void observeViewModel() {
        viewModel.getGroup().observe(this, group -> {
            if (group != null) {
                binding.toolbar.setTitle(group.getName());
            }
        });

        viewModel.getTotalGroupSpending().observe(this, total -> {
            double amount = total != null ? total : 0.0;
            binding.tvTotalGroupSpending.setText(currencyFormatter.format(amount));
        });

        viewModel.getGroupMembers().observe(this, members -> {
            if (members != null) {
                debtAdapter.setMembers(members);
                sharedExpenseAdapter.setMembers(members);
            }
        });

        viewModel.getSimplifiedDebts().observe(this, debts -> {
            debtAdapter.setDebts(debts);
            boolean empty = (debts == null || debts.isEmpty());
            binding.tvNoDebts.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvDebts.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getMemberSummaries().observe(this, summaries -> {
            memberSummaryAdapter.setSummaries(summaries);
        });

        viewModel.getGroupExpenses().observe(this, expenses -> {
            sharedExpenseAdapter.setExpenses(expenses);
            boolean empty = (expenses == null || expenses.isEmpty());
            binding.tvNoExpenses.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvGroupExpenses.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void setupActions() {
        binding.btnAddMember.setOnClickListener(v -> {
            AddFriendBottomSheetDialogFragment dialog = AddFriendBottomSheetDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "add_friend");
        });

        binding.fabAddSharedExpense.setOnClickListener(v -> {
            AddSharedExpenseBottomSheetDialogFragment dialog = AddSharedExpenseBottomSheetDialogFragment.newInstance();
            dialog.show(getSupportFragmentManager(), "add_shared_expense");
        });
    }
}
