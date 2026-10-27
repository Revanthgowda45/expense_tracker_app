package com.example.studentexpensetracker.ui.scanner;

import android.app.Application;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentexpensetracker.data.local.AppExecutors;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.repository.TransactionRepository;
import com.example.studentexpensetracker.domain.scanner.ParsedReceipt;
import com.example.studentexpensetracker.domain.scanner.ReceiptParser;
import com.example.studentexpensetracker.domain.scanner.ReceiptStorageManager;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ReceiptScannerViewModel extends AndroidViewModel {

    private static final String TAG = "ReceiptScannerVM";

    private final TransactionRepository transactionRepository;
    private final TextRecognizer recognizer;

    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<String> processingStatus = new MutableLiveData<>("Ready to scan");
    private final MutableLiveData<String> scanError = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isFlashOn = new MutableLiveData<>(false);
    private final MutableLiveData<ParsedReceipt> extractedReceipt = new MutableLiveData<>(null);

    public ReceiptScannerViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = TransactionRepository.getInstance(application);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public LiveData<String> getProcessingStatus() {
        return processingStatus;
    }

    public LiveData<String> getScanError() {
        return scanError;
    }

    public LiveData<Boolean> getIsFlashOn() {
        return isFlashOn;
    }

    public LiveData<ParsedReceipt> getExtractedReceipt() {
        return extractedReceipt;
    }

    public void toggleFlash() {
        Boolean cur = isFlashOn.getValue();
        isFlashOn.setValue(cur != null && !cur);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public void processCameraImage(ImageProxy imageProxy, Context context) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            scanError.postValue("Failed to capture image frame");
            isProcessing.postValue(false);
            imageProxy.close();
            return;
        }

        isProcessing.postValue(true);
        processingStatus.postValue("Saving receipt & analyzing with on-device AI...");
        scanError.postValue(null);

        AppExecutors.getInstance().diskIO().execute(() -> {
            String savedImagePath = ReceiptStorageManager.saveImageProxy(context, imageProxy);

            try {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                recognizer.process(image)
                        .addOnSuccessListener(visionText -> {
                            handleOcrSuccess(visionText.getText(), savedImagePath);
                            imageProxy.close();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "OCR failed", e);
                            isProcessing.postValue(false);
                            scanError.postValue("Could not recognize text. Please try again with better lighting.");
                            imageProxy.close();
                        });
            } catch (Exception e) {
                Log.e(TAG, "Image conversion failed", e);
                isProcessing.postValue(false);
                scanError.postValue("Failed to process image frame");
                imageProxy.close();
            }
        });
    }

    public void processGalleryUri(Uri uri, Context context) {
        isProcessing.postValue(true);
        processingStatus.postValue("Importing image & analyzing with on-device AI...");
        scanError.postValue(null);

        AppExecutors.getInstance().diskIO().execute(() -> {
            String savedImagePath = ReceiptStorageManager.saveUri(context, uri);

            try {
                InputImage inputImage = InputImage.fromFilePath(context, uri);
                recognizer.process(inputImage)
                        .addOnSuccessListener(visionText -> handleOcrSuccess(visionText.getText(), savedImagePath))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "OCR failed for gallery image", e);
                            isProcessing.postValue(false);
                            scanError.postValue("Could not read text from selected photo.");
                        });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load image from URI", e);
                isProcessing.postValue(false);
                scanError.postValue("Failed to load selected photo.");
            }
        });
    }

    private void handleOcrSuccess(String ocrText, String imagePath) {
        ParsedReceipt parsed = ReceiptParser.parse(ocrText, imagePath);
        AppExecutors.getInstance().mainThread().execute(() -> {
            isProcessing.setValue(false);
            extractedReceipt.setValue(parsed);
        });
    }

    public void clearDraft() {
        ParsedReceipt parsed = extractedReceipt.getValue();
        if (parsed != null && parsed.getReceiptImagePath() != null) {
            ReceiptStorageManager.deleteReceiptImage(parsed.getReceiptImagePath());
        }
        extractedReceipt.setValue(null);
        isProcessing.setValue(false);
        scanError.setValue(null);
    }

    public boolean saveExtractedReceipt(
            String amountStr,
            String merchant,
            String category,
            long dateMillis,
            String paymentMethod,
            String mood,
            String receiptImagePath,
            Runnable onSuccess) {
        if (amountStr == null || amountStr.trim().isEmpty()) return false;
        double amount;
        try {
            amount = Double.parseDouble(amountStr.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (amount <= 0) return false;

        String title = (merchant != null && !merchant.trim().isEmpty()) ? merchant.trim() : "";
        String cat = (category != null && !category.trim().isEmpty()) ? category.trim() : "Food & Dining";
        String finalNote = !title.isEmpty() ? cat + ": " + title : cat;

        TransactionEntity entity = new TransactionEntity(
                amount,
                "EXPENSE",
                1L,
                finalNote,
                paymentMethod != null ? paymentMethod : "UPI",
                dateMillis > 0 ? dateMillis : System.currentTimeMillis(),
                false,
                null,
                System.currentTimeMillis(),
                mood,
                receiptImagePath
        );

        transactionRepository.insertTransaction(entity, onSuccess);
        return true;
    }
}
