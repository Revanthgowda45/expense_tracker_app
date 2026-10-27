package com.example.studentexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.studentexpensetracker.databinding.ActivityMainBinding;
import com.example.studentexpensetracker.ui.home.HomeFragment;
import com.example.studentexpensetracker.ui.reports.ReportsFragment;
import com.example.studentexpensetracker.ui.scanner.ReceiptScannerActivity;
import com.example.studentexpensetracker.ui.settings.SettingsFragment;
import com.example.studentexpensetracker.ui.split.SplitFragment;
import com.example.studentexpensetracker.ui.transactions.AddEditTransactionActivity;
import com.example.studentexpensetracker.ui.transactions.TransactionsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupFabs();

        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance(), false);
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment = null;
            boolean showFabs = true;

            if (itemId == R.id.nav_home) {
                fragment = HomeFragment.newInstance();
                showFabs = true;
            } else if (itemId == R.id.nav_transactions) {
                fragment = TransactionsFragment.newInstance();
                showFabs = true;
            } else if (itemId == R.id.nav_reports) {
                fragment = ReportsFragment.newInstance();
                showFabs = true;
            } else if (itemId == R.id.nav_split) {
                fragment = SplitFragment.newInstance();
                showFabs = false; // Split screen has its own group creation FAB
            } else if (itemId == R.id.nav_settings) {
                fragment = SettingsFragment.newInstance();
                showFabs = false;
            }

            if (fragment != null) {
                loadFragment(fragment, false);
                toggleFabs(showFabs);
                return true;
            }
            return false;
        });
    }

    private void setupFabs() {
        binding.fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditTransactionActivity.class);
            startActivity(intent);
        });

        binding.fabScanReceipt.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceiptScannerActivity.class);
            startActivity(intent);
        });
    }

    private void toggleFabs(boolean visible) {
        if (visible) {
            binding.fabAddTransaction.show();
            binding.fabScanReceipt.show();
        } else {
            binding.fabAddTransaction.hide();
            binding.fabScanReceipt.hide();
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void navigateToTab(int tabIndex) {
        switch (tabIndex) {
            case 0:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
            case 1:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_transactions);
                break;
            case 2:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_reports);
                break;
            case 3:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_split);
                break;
            case 4:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
                break;
        }
    }
}
