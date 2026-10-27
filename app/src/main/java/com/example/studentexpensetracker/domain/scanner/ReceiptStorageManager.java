package com.example.studentexpensetracker.domain.scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import androidx.camera.core.ImageProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ReceiptStorageManager {

    private static final int MAX_DIMENSION = 1920;
    private static final int JPEG_QUALITY = 85;

    public static String saveImageProxy(Context context, ImageProxy imageProxy) {
        try {
            Bitmap bitmap = imageProxy.toBitmap();
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            Bitmap rotatedBitmap = rotationDegrees != 0 ? rotateBitmap(bitmap, rotationDegrees) : bitmap;
            return saveBitmap(context, rotatedBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveUri(Context context, Uri uri) {
        try {
            float rotation = 0f;
            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                if (stream != null) {
                    ExifInterface exif = new ExifInterface(stream);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        rotation = 90f;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        rotation = 180f;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotation = 270f;
                    }
                }
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                if (stream != null) {
                    BitmapFactory.decodeStream(stream, null, options);
                }
            }

            int inSampleSize = 1;
            if (options.outHeight > MAX_DIMENSION || options.outWidth > MAX_DIMENSION) {
                int halfHeight = options.outHeight / 2;
                int halfWidth = options.outWidth / 2;
                while ((halfHeight / inSampleSize) >= MAX_DIMENSION && (halfWidth / inSampleSize) >= MAX_DIMENSION) {
                    inSampleSize *= 2;
                }
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = inSampleSize;

            Bitmap bitmap;
            try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
                if (stream == null) return null;
                bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions);
            }

            if (bitmap == null) return null;

            Bitmap finalBitmap = rotation != 0f ? rotateBitmap(bitmap, rotation) : bitmap;
            return saveBitmap(context, finalBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveBitmap(Context context, Bitmap bitmap) {
        try {
            File receiptsDir = new File(context.getFilesDir(), "receipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }
            String fileName = "receipt_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + ".jpg";
            File file = new File(receiptsDir, fileName);

            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteReceiptImage(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
