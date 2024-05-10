package com.appsonair;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.view.View;

import androidx.core.app.ActivityCompat;

import java.io.InputStream;


public class ScreenshotDetector {

    private final Context context;
    private ContentObserver contentObserver;
    private String previousPath = "";
    private static final String TAG = "DetectorModule";


    public ScreenshotDetector(Context context) {
        this.context = context;
    }

    public void startScreenshotDetection() {
        if (!isReadMediaPermissionGranted()) {
            // If permission is not granted, request it

            requestReadMediaPermission();
        }
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Log.d(TAG, "isReadMediaPermissionGranted->> " + isReadMediaPermissionGranted());
                if (isReadMediaPermissionGranted() && uri != null) {
                    String path = getFilePathFromContentResolver(context, uri);
                    if (isScreenshotPath(path)) {
                        previousPath = path;
                        onScreenCaptured();
                    }
                } else {
                    showNativeModal();
                }
            }
        };

        context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
        );
    }

    private void showNativeModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Screenshot Captured")
                .setMessage("Permission denied for reading media images")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void requestReadMediaPermission() {
        // Request permission from the user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
            ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_MEDIA_PERMISSION);
        }

    }

    // Add this constant at the beginning of the class
    private static final int REQUEST_MEDIA_PERMISSION = 123; // Use any unique value

    // Override onRequestPermissionsResult in your activity or fragment to handle the result


    public void stopScreenshotDetection() {
        context.getContentResolver().unregisterContentObserver(Objects.requireNonNull(contentObserver));
    }

    private void onScreenCaptured() {
        Bitmap screenshotBitmap = takeNativeScreenshot();
        String screenshotPath = saveBitmapToFile(screenshotBitmap);

        moveToFullScreen(screenshotPath);
    }

    private Bitmap takeNativeScreenshot() {
        try {
            // Get the root view of the activity
            View rootView = ((Activity) context).getWindow().getDecorView().getRootView();

            // Enable drawing cache
            rootView.setDrawingCacheEnabled(true);

            // Create a bitmap from the drawing cache
            Bitmap screenshotBitmap = Bitmap.createBitmap(rootView.getDrawingCache());

            // Disable drawing cache to release resources
            rootView.setDrawingCacheEnabled(false);

            return screenshotBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error capturing screenshot", e);
            return null;
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File cacheDir = context.getCacheDir();
            String fileName = "NativeScreenshot_" + getCurrentDateTimeString() + ".jpg";
            File screenshotFile = new File(cacheDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(screenshotFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return screenshotFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving native screenshot to file", e);
            return null;
        }
    }


    public void moveToFullScreen(final String imagePath) {
        //Set and copy path
        Log.d(TAG, "showBugReportDialog:---> " + imagePath);
        File originalImageFile = new File(imagePath);

        Log.d(TAG, "originalImageFile:---> " + originalImageFile);

        if (!originalImageFile.exists()) {
            Log.e(TAG, "Original file does not exist: " + imagePath);
            return;
        }

        // Create a new File object in the cache directory
        File cacheDir = context.getCacheDir();
        String newFileName = "AppsOnAir_Services_Screenshot" + getCurrentDateTimeString() + ".jpg";
        File newImageFile = new File(cacheDir, newFileName);

        // Copy the original image to the cache directory
        try {
            copyFile(originalImageFile, newImageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Convert the File to a content URI
        Uri imageUri = Uri.fromFile(newImageFile);

        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra("IMAGE_PATH", imageUri);
        context.startActivity(intent);
    }

    private boolean isScreenshotPath(String path) {
        return path != null && path.toLowerCase().contains("screenshots") && !previousPath.equals(path);
    }

    private String getCurrentDateTimeString() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private String getFilePathFromContentResolver(Context context, Uri uri) {
        try {
            String[] projection = {
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
            };
            android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range")
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
                return path;
            }
        } catch (Exception e) {
            // Handle the exception appropriately
        }
        return null;
    }

    private String[] mediaImagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        }
        return new String[0];
    }


    private boolean isReadMediaPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Build.VERSION_CODES.M 2=>>>>--->>>>>>===>>> " + Build.VERSION_CODES.M);

            return ContextCompat.checkSelfPermission(context, mediaImagesPermissions()[0]) == PackageManager.PERMISSION_GRANTED;
        } else {
            // For Android versions 13 and above, check READ_MEDIA_IMAGES permission

            Log.d(TAG, "Build.VERSION_CODES.M =>>>>--->>>>>>===>>> " + Build.VERSION_CODES.M);
            // For Android versions less than 13, check READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void copyFile(File source, File destination) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
