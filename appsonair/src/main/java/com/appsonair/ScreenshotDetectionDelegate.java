package com.appsonair;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import org.apache.commons.io.FileUtils;

import java.io.InputStream;


public class ScreenshotDetectionDelegate {

    private final Context context;
    //    private final ScreenshotDetectionListener listener;
    private ContentObserver contentObserver;

    private boolean isListening = false;
    private String previousPath = "";
    private boolean isDialogOpen = false;
    private static final String TAG = "DetectorModule";

    private AlertDialog bugReportDialog; // New variable to hold the reference to the bug report dialog


    public ScreenshotDetectionDelegate(Context context) {
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
                    if (path != null && isScreenshotPath(path)) {
                        previousPath = path;
                        onScreenCaptured(path);
                    }
                } else {
                    onScreenCapturedWithDeniedPermission();
                }
            }
        };

        context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
        );
        isListening = true;
    }

    private void requestReadMediaPermission() {
        // Request permission from the user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_MEDIA_PERMISSION);
        }

    }

    // Add this constant at the beginning of the class
    private static final int REQUEST_MEDIA_PERMISSION = 123; // Use any unique value

    // Override onRequestPermissionsResult in your activity or fragment to handle the result


    public void stopScreenshotDetection() {
        context.getContentResolver().unregisterContentObserver(Objects.requireNonNull(contentObserver));
        isListening = false;
    }

    private void onScreenCaptured(String path) {
        Log.d(TAG, "onScreenCaptured called"); // Log this line

        // Show native modal only if the dialog is not already open
        if (!isDialogOpen) {
//            showNativeModal(path);
//            editScreenshotWithPhotoEditor(context,path);
            Log.d(TAG, "onScreenCaptured called");

            // Take a native screenshot and save it to a file
            Bitmap screenshotBitmap = takeNativeScreenshot();
            String screenshotPath = saveBitmapToFile(screenshotBitmap);

            // Show Bug Report Dialog with the native screenshot path
            showBugReportDialog(screenshotPath);
        }
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

    private void showBugReportDialog(final String imagePath) {
        // Check if the bug report dialog is already showing
        if (bugReportDialog != null && bugReportDialog.isShowing()) {
            updateBugReportDialog(imagePath);
        } else {
            // Bug report dialog is not showing, create and show a new one
            createAndShowBugReportDialog(imagePath);
        }
    }


    public void updateBugReportDialog(final String imagePath) {
        if (bugReportDialog != null) {
            // Update the image in the existing dialog
            ImageView screenshotImageView = bugReportDialog.findViewById(R.id.screenshotImageView);
            if (screenshotImageView != null) {
                Uri imageUri = Uri.fromFile(new File(imagePath));
                screenshotImageView.setImageURI(imageUri);
            }
        }
    }

    public void createAndShowBugReportDialog(final String imagePath) {
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

        // Use ContentResolver to open the input stream


        //Set dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_bug_report, null);
        builder.setView(dialogView);


        final EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        final EditText bugInfoEditText = dialogView.findViewById(R.id.bugInfoEditText);
        final ImageView screenshotImageView = dialogView.findViewById(R.id.screenshotImageView);
//            final ImageButton editScreenshotButton = dialogView.findViewById(R.id.editScreenshotButton);
        final ImageButton closeScreenshotButton = dialogView.findViewById(R.id.closeScreenshotButton);
        final Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        final Button submitButton = dialogView.findViewById(R.id.submitButton);

        isDialogOpen = true;
        bugReportDialog = builder.create();
        bugReportDialog.show();

        screenshotImageView.setImageURI(imageUri);
        screenshotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullscreenView(imageUri);
            }
        });

//            editScreenshotButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // Handle edit screenshot button click
//                    editScreenshotWithPhotoEditor(context, imageUri);
//                }
//            });

        closeScreenshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle close screenshot button click
                isDialogOpen = false;
                bugReportDialog.dismiss();
            }
        });

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEditText.getText().toString();
                String bugInfo = bugInfoEditText.getText().toString();
                // Handle submission logic here

                // Close the dialog
                isDialogOpen = false;
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog
                isDialogOpen = false;
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close the dialog
                isDialogOpen = false;
                bugReportDialog.dismiss();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Close the dialog
                isDialogOpen = false;
                bugReportDialog.dismiss();
            }
        });


    }

    private void openFullscreenView(Uri imagePath) {
        // Start FullscreenActivity with the image path
        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra("IMAGE_PATH", imagePath);
        context.startActivity(intent);
    }


    private void onScreenCapturedWithDeniedPermission() {
        if (!isDialogOpen) {
            showNativeModal("Permission denied for reading media images");
        }
    }

    private boolean isScreenshotPath(String path) {
        return path != null && path.toLowerCase().contains("screenshots") && !previousPath.equals(path);
    }

    private void editScreenshotWithPhotoEditor(Context context, String imagePath) {
        Log.d(TAG, "editScreenshotWithPhotoEditor called");
        try {
            // Create a File object from the imagePath
            File imageFile = new File(imagePath);

            // Convert the File to a content URI
            Uri imageUri = Uri.fromFile(imageFile);

            // Use ContentResolver to open the input stream
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

            if (inputStream != null) {
                // If the inputStream is not null, proceed to open the EditImageActivity
                Intent intent = new Intent(context, EditImageActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(imageUri, "image/*");
                context.startActivity(intent);
            } else {
                Log.e(TAG, "InputStream is null for URI: " + imageUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "editScreenshotWithPhotoEditor " + e);
            e.printStackTrace();
        }
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
        return new String[]{
                Manifest.permission.READ_MEDIA_IMAGES
        };
    }


    private boolean isReadMediaPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Build.VERSION_CODES.M 2=>>>>--->>>>>>===>>> " + Build.VERSION_CODES.M);

            if (ContextCompat.checkSelfPermission(context, mediaImagesPermissions()[0]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
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


    public void setDialogOpen(boolean bool) {
        this.isDialogOpen = bool;
    }

    private void showNativeModal(final String path) {
        isDialogOpen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Screenshot Captured")
                .setMessage("Screenshot captured at path: " + path)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        isDialogOpen = false; // Reset the flag when the dialog is dismissed
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
