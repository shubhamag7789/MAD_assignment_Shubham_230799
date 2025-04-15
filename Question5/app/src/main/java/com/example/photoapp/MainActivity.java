package com.example.photoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_DIRECTORY = 100;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 101;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private Uri chosenFolderUri;
    private Uri photoUri;

    Button btnSelectFolder, btnTakePhoto, btnOpenGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);

        btnSelectFolder.setOnClickListener(v -> selectFolder());

        btnTakePhoto.setOnClickListener(v -> {
            // Check for CAMERA permission at runtime.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                dispatchTakePictureIntent();
            }
        });

        btnOpenGallery.setOnClickListener(v -> {
            if (chosenFolderUri != null) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                intent.putExtra("folderUri", chosenFolderUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "No folder selected yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Launch the folder picker using Storage Access Framework
    private void selectFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    // Create a temporary image file for the captured photo
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Launch the camera intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE);
                }
            } catch (IOException ex) {
                Toast.makeText(this, "File creation failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle results from folder picker and camera capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == RESULT_OK && data != null) {
            chosenFolderUri = data.getData();
            int flags = data.getFlags();
            if (flags == 0) {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            } else {
                flags = flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (flags == 0) {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                }
            }
            try {
                getContentResolver().takePersistableUriPermission(chosenFolderUri, flags);
                Toast.makeText(this, "Folder selected successfully", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(this, "Permission error. Try again.", Toast.LENGTH_SHORT).show();
            }
            if (photoUri != null) {
                savePhotoToFolder();
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (chosenFolderUri == null) {
                    new AlertDialog.Builder(this)
                            .setTitle("No Folder Selected")
                            .setMessage("Please select a folder to save the captured image.")
                            .setPositiveButton("Select Folder", (dialog, which) -> selectFolder())
                            .setNegativeButton("Cancel", (dialog, which) ->
                                    Toast.makeText(MainActivity.this, "Image not saved", Toast.LENGTH_SHORT).show())
                            .show();
                } else {
                    savePhotoToFolder();
                }
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Save the captured photo to the selected folder using DocumentFile API
    private void savePhotoToFolder() {
        if (chosenFolderUri == null || photoUri == null)
            return;

        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, chosenFolderUri);
        if (pickedDir == null) {
            Toast.makeText(this, "Invalid folder", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream in = getContentResolver().openInputStream(photoUri);
            DocumentFile newFile = pickedDir.createFile("image/jpeg", "IMG_" + System.currentTimeMillis() + ".jpg");
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
            photoUri = null;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }
}
