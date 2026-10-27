package com.example.studentexpensetracker.ui.transactions;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.R;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.databinding.ActivityAddEditTransactionBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_MERCHANT = "merchant";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_RECEIPT_PATH = "receipt_path";

    private ActivityAddEditTransactionBinding binding;
    private AddEditTransactionViewModel viewModel;

    private long editingTransactionId = -1L;
    private long selectedDateMillis = System.currentTimeMillis();
    private String receiptImagePath = null;
    private TransactionEntity loadedEntity = null;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AddEditTransactionViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        editingTransactionId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, -1L);

        setupDatePicker();
        setupCategoryToggle();
        setupSaveButton();

        if (editingTransactionId > 0) {
            binding.toolbar.setTitle("Edit Transaction");
            binding.btnSave.setText("Update Transaction");
            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());

            viewModel.loadTransaction(editingTransactionId);
            viewModel.getCurrentTransaction().observe(this, this::populateFromEntity);
        } else {
            binding.toolbar.setTitle("Add Transaction");
            populateFromPrefill();
        }
    }

    private void setupDatePicker() {
        binding.tvSelectedDate.setText(dateFormat.format(new Date(selectedDateMillis)));

        binding.rowDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDateMillis);
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth);
                        selectedDateMillis = selected.getTimeInMillis();
                        binding.tvSelectedDate.setText(dateFormat.format(new Date(selectedDateMillis)));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void setupCategoryToggle() {
        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTypeIncome) {
                    binding.chipFood.setText("Salary / Job");
                    binding.chipTransport.setText("Stipend");
                    binding.chipBooks.setText("Pocket Money");
                    binding.chipShopping.setText("Grants / Scholarship");
                    binding.chipTuition.setText("Freelance");
                    binding.chipAllowance.setText("Gift / Award");
                } else {
                    binding.chipFood.setText("Food & Dining");
                    binding.chipTransport.setText("Transport");
                    binding.chipBooks.setText("Books & Supplies");
                    binding.chipShopping.setText("Shopping");
                    binding.chipTuition.setText("Fees & Tuition");
                    binding.chipAllowance.setText("Allowance");
                }
            }
        });

        binding.btnRemoveReceipt.setOnClickListener(v -> {
            receiptImagePath = null;
            binding.cardReceiptAttachment.setVisibility(View.GONE);
        });
    }

    private void populateFromPrefill() {
        String prefillAmount = getIntent().getStringExtra(EXTRA_AMOUNT);
        String prefillMerchant = getIntent().getStringExtra(EXTRA_MERCHANT);
        String prefillDate = getIntent().getStringExtra(EXTRA_DATE);
        String prefillCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
        String prefillReceipt = getIntent().getStringExtra(EXTRA_RECEIPT_PATH);

        if (prefillAmount != null && !prefillAmount.isEmpty()) {
            binding.etAmount.setText(prefillAmount);
        }
        if (prefillMerchant != null && !prefillMerchant.isEmpty()) {
            binding.etNote.setText(prefillMerchant);
        }
        if (prefillDate != null && !prefillDate.isEmpty()) {
            try {
                selectedDateMillis = Long.parseLong(prefillDate);
                binding.tvSelectedDate.setText(dateFormat.format(new Date(selectedDateMillis)));
            } catch (NumberFormatException ignored) {}
        }
        if (prefillCategory != null && !prefillCategory.isEmpty()) {
            selectCategoryChip(prefillCategory);
        }
        if (prefillReceipt != null && !prefillReceipt.isEmpty()) {
            receiptImagePath = prefillReceipt;
            showReceiptPreview(receiptImagePath);
        }
    }

    private void populateFromEntity(TransactionEntity entity) {
        if (entity == null) return;
        loadedEntity = entity;

        if (entity.getAmount() % 1.0 == 0.0) {
            binding.etAmount.setText(String.valueOf((long) entity.getAmount()));
        } else {
            binding.etAmount.setText(String.valueOf(entity.getAmount()));
        }

        if ("INCOME".equalsIgnoreCase(entity.getType())) {
            binding.toggleType.check(R.id.btnTypeIncome);
        } else {
            binding.toggleType.check(R.id.btnTypeExpense);
        }

        String note = entity.getNote();
        if (note != null && !note.isEmpty()) {
            int colonIdx = note.indexOf(':');
            if (colonIdx > 0) {
                String cat = note.substring(0, colonIdx).trim();
                String desc = note.substring(colonIdx + 1).trim();
                selectCategoryChip(cat);
                binding.etNote.setText(desc);
            } else {
                binding.etNote.setText(note);
            }
        }

        selectedDateMillis = entity.getDateTime();
        binding.tvSelectedDate.setText(dateFormat.format(new Date(selectedDateMillis)));

        selectMoodChip(entity.getMood());
        selectPaymentChip(entity.getPaymentMethod());

        if (entity.getReceiptImagePath() != null && !entity.getReceiptImagePath().trim().isEmpty()) {
            receiptImagePath = entity.getReceiptImagePath();
            showReceiptPreview(receiptImagePath);
        }
    }

    private void showReceiptPreview(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) {
            binding.cardReceiptAttachment.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                binding.ivReceiptThumb.setImageBitmap(bitmap);
            }
        }
    }

    private void selectCategoryChip(String category) {
        if (category == null) return;
        for (int i = 0; i < binding.chipGroupCategory.getChildCount(); i++) {
            View v = binding.chipGroupCategory.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.getText().toString().toLowerCase().contains(category.toLowerCase())) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    private void selectMoodChip(String mood) {
        if (mood == null) return;
        for (int i = 0; i < binding.chipGroupMood.getChildCount(); i++) {
            View v = binding.chipGroupMood.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.getText().toString().toLowerCase().contains(mood.toLowerCase())) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    private void selectPaymentChip(String payment) {
        if (payment == null) return;
        for (int i = 0; i < binding.chipGroupPayment.getChildCount(); i++) {
            View v = binding.chipGroupPayment.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.getText().toString().equalsIgnoreCase(payment)) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String amount = binding.etAmount.getText() != null ? binding.etAmount.getText().toString().trim() : "";
            if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isIncome = binding.toggleType.getCheckedButtonId() == R.id.btnTypeIncome;
            String type = isIncome ? "INCOME" : "EXPENSE";

            String category = getSelectedCategory();
            String note = binding.etNote.getText() != null ? binding.etNote.getText().toString().trim() : "";
            String mood = getSelectedMood();
            String paymentMethod = getSelectedPaymentMethod();

            boolean success;
            if (editingTransactionId > 0) {
                success = viewModel.updateTransaction(
                        editingTransactionId,
                        amount,
                        type,
                        category,
                        selectedDateMillis,
                        note,
                        mood,
                        paymentMethod,
                        receiptImagePath,
                        this::finish
                );
            } else {
                success = viewModel.saveTransaction(
                        amount,
                        type,
                        category,
                        selectedDateMillis,
                        note,
                        mood,
                        paymentMethod,
                        receiptImagePath,
                        this::finish
                );
            }

            if (!success) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedCategory() {
        int checkedId = binding.chipGroupCategory.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = findViewById(checkedId);
            if (chip != null) return chip.getText().toString();
        }
        return "Food & Dining";
    }

    private String getSelectedMood() {
        int checkedId = binding.chipGroupMood.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                String text = chip.getText().toString();
                // Extract mood name after emoji (e.g. "😊 Happy" -> "Happy")
                if (text.contains(" ")) {
                    return text.substring(text.indexOf(' ') + 1).trim();
                }
                return text;
            }
        }
        return null;
    }

    private String getSelectedPaymentMethod() {
        int checkedId = binding.chipGroupPayment.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip chip = findViewById(checkedId);
            if (chip != null) return chip.getText().toString();
        }
        return "UPI";
    }

    private void showDeleteConfirmation() {
        if (loadedEntity == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to permanently delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTransaction(loadedEntity, this::finish);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
