package com.example.studentexpensetracker.ui.scanner

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.repository.TransactionRepository
import com.example.studentexpensetracker.domain.scanner.ParsedReceipt
import com.example.studentexpensetracker.domain.scanner.ReceiptParser
import com.example.studentexpensetracker.domain.scanner.ReceiptStorageManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isProcessing: Boolean = false,
    val processingStatus: String = "Reading receipt...",
    val scanError: String? = null,
    val isFlashOn: Boolean = false,
    val isReviewing: Boolean = false,
    val showFullImageDialog: Boolean = false,

    // Extracted & Editable Fields
    val amount: String = "",
    val merchant: String = "",
    val category: String = "Food & Dining",
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI",
    val mood: String? = "😊",
    val note: String = "",
    val receiptImagePath: String? = null,
    val rawOcrText: String = ""
)

@HiltViewModel
class ReceiptScannerViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    fun processCameraImage(imageProxy: ImageProxy, context: Context) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            _uiState.update { it.copy(scanError = "Failed to capture image frame", isProcessing = false) }
            imageProxy.close()
            return
        }

        _uiState.update { 
            it.copy(
                isProcessing = true, 
                processingStatus = "Saving receipt & analyzing with on-device AI...", 
                scanError = null 
            ) 
        }

        viewModelScope.launch {
            // 1. Save rotated image to app-private storage
            val savedImagePath = ReceiptStorageManager.saveImageProxy(context, imageProxy)

            // 2. Process with ML Kit OCR
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    handleOcrSuccess(visionText.text, savedImagePath)
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerVM", "OCR failed", e)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            scanError = "Could not recognize text. Please try again with better lighting."
                        )
                    }
                    imageProxy.close()
                }
        }
    }

    fun processGalleryUri(uri: Uri, context: Context) {
        _uiState.update { 
            it.copy(
                isProcessing = true, 
                processingStatus = "Importing image & analyzing with on-device AI...", 
                scanError = null 
            ) 
        }

        viewModelScope.launch {
            // 1. Save and compress into app-private storage
            val savedImagePath = ReceiptStorageManager.saveUri(context, uri)

            // 2. Process with ML Kit OCR
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        handleOcrSuccess(visionText.text, savedImagePath)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ScannerVM", "OCR failed for gallery image", e)
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                scanError = "Could not read text from selected photo."
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("ScannerVM", "Failed to load image from URI", e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        scanError = "Failed to load selected photo."
                    )
                }
            }
        }
    }

    private fun handleOcrSuccess(ocrText: String, imagePath: String?) {
        val parsed = ReceiptParser.parse(ocrText, imagePath)
        
        _uiState.update { state ->
            state.copy(
                isProcessing = false,
                isReviewing = true,
                amount = parsed.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                merchant = parsed.merchantName.orEmpty(),
                category = parsed.suggestedCategory ?: "Food & Dining",
                dateMillis = parsed.dateMillis ?: System.currentTimeMillis(),
                receiptImagePath = imagePath,
                rawOcrText = ocrText
            )
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(merchant = merchant) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateDateMillis(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun updatePaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun updateMood(mood: String?) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun setFullImageDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showFullImageDialog = visible) }
    }

    fun retake() {
        // Discard current draft and return to viewfinder
        val currentImage = _uiState.value.receiptImagePath
        viewModelScope.launch {
            ReceiptStorageManager.deleteReceiptImage(currentImage)
        }
        _uiState.update { 
            ScannerUiState(
                isFlashOn = it.isFlashOn
            ) 
        }
    }

    fun saveExpense(onSuccess: () -> Unit): Boolean {
        val state = _uiState.value
        val amountDouble = state.amount.toDoubleOrNull() ?: return false
        if (amountDouble <= 0) return false

        val merchantTitle = state.merchant.trim()
        val finalNote = if (merchantTitle.isNotBlank()) "${state.category}: $merchantTitle" else state.category

        viewModelScope.launch {
            transactionRepository.insertTransaction(
                TransactionEntity(
                    amount = amountDouble,
                    type = "EXPENSE",
                    categoryId = 1,
                    note = finalNote,
                    paymentMethod = state.paymentMethod,
                    dateTime = state.dateMillis,
                    isRecurring = false,
                    recurrenceType = null,
                    createdAt = System.currentTimeMillis(),
                    mood = state.mood,
                    receiptImagePath = state.receiptImagePath
                )
            )
            onSuccess()
        }
        return true
    }
}
