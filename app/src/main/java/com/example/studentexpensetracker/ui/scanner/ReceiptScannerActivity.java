package com.example.studentexpensetracker.ui.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentexpensetracker.R;
import com.example.studentexpensetracker.databinding.ActivityReceiptScannerBinding;
import com.example.studentexpensetracker.domain.scanner.ParsedReceipt;
import com.example.studentexpensetracker.ui.transactions.AddEditTransactionActivity;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptScannerActivity extends AppCompatActivity {

    private ActivityReceiptScannerBinding binding;
    private ReceiptScannerViewModel viewModel;
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan physical receipts", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.processGalleryUri(uri, this);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReceiptScannerViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.btnClose.setOnClickListener(v -> finish());

        binding.btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.btnFlash.setOnClickListener(v -> viewModel.toggleFlash());

        binding.btnCapture.setOnClickListener(v -> takePhoto());

        setupObservers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupObservers() {
        viewModel.getIsProcessing().observe(this, processing -> {
            binding.layoutProcessing.setVisibility(processing ? View.VISIBLE : View.GONE);
        });

        viewModel.getProcessingStatus().observe(this, status -> {
            if (status != null) binding.tvProcessingStatus.setText(status);
        });

        viewModel.getScanError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsFlashOn().observe(this, flashOn -> {
            binding.btnFlash.setImageResource(flashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(flashOn);
            }
        });

        viewModel.getExtractedReceipt().observe(this, this::bindReviewCard);
    }

    private void bindReviewCard(ParsedReceipt receipt) {
        if (receipt == null) {
            binding.cardReview.setVisibility(View.GONE);
            binding.bottomControls.setVisibility(View.VISIBLE);
            return;
        }

        binding.cardReview.setVisibility(View.VISIBLE);
        binding.bottomControls.setVisibility(View.GONE);

        String merchant = receipt.getMerchantName() != null ? receipt.getMerchantName() : "Receipt Expense";
        binding.tvReviewMerchant.setText(merchant);

        Double amount = receipt.getAmount();
        binding.tvReviewAmount.setText(amount != null ? currencyFormatter.format(amount) : "₹0.00");

        String cat = receipt.getSuggestedCategory() != null ? receipt.getSuggestedCategory() : "Food & Dining";
        String dateStr = dateFormat.format(new Date(receipt.getDateMillis() != null ? receipt.getDateMillis() : System.currentTimeMillis()));
        binding.tvReviewMeta.setText("Category: " + cat + " • Date: " + dateStr);

        binding.btnReviewSave.setOnClickListener(v -> {
            String amountStr = amount != null ? String.valueOf(amount) : "0";
            viewModel.saveExtractedReceipt(
                    amountStr,
                    merchant,
                    cat,
                    receipt.getDateMillis() != null ? receipt.getDateMillis() : System.currentTimeMillis(),
                    "UPI",
                    "😊",
                    receipt.getReceiptImagePath(),
                    this::finish
            );
        });

        binding.btnReviewEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditTransactionActivity.class);
            if (amount != null) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_AMOUNT, String.valueOf(amount));
            }
            if (merchant != null) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_MERCHANT, merchant);
            }
            if (receipt.getDateMillis() != null) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_DATE, String.valueOf(receipt.getDateMillis()));
            }
            intent.putExtra(AddEditTransactionActivity.EXTRA_CATEGORY, cat);
            if (receipt.getReceiptImagePath() != null) {
                intent.putExtra(AddEditTransactionActivity.EXTRA_RECEIPT_PATH, receipt.getReceiptImagePath());
            }
            startActivity(intent);
            finish();
        });

        binding.btnReviewRetake.setOnClickListener(v -> viewModel.clearDraft());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                viewModel.processCameraImage(image, ReceiptScannerActivity.this);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ReceiptScannerActivity.this, "Failed to capture photo", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        binding = null;
    }
}
