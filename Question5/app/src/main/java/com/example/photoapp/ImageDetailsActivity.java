package com.example.photoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class ImageDetailsActivity extends AppCompatActivity {

    private ImageView imageViewDetail;
    private TextView tvImageName, tvImagePath, tvImageSize;
    private Button btnDelete;
    private Uri imageUri;
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        imageViewDetail = findViewById(R.id.imageViewDetail);
        tvImageName = findViewById(R.id.tvImageName);
        tvImagePath = findViewById(R.id.tvImagePath);
        tvImageSize = findViewById(R.id.tvImageSize);
        btnDelete = findViewById(R.id.btnDelete);

        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        folderUri = Uri.parse(getIntent().getStringExtra("folderUri"));

        // Display the image clearly
        imageViewDetail.setImageURI(imageUri);

        DocumentFile file = DocumentFile.fromSingleUri(this, imageUri);
        if (file != null) {
            tvImageName.setText("Name: " + file.getName());
            tvImagePath.setText("Path: " + file.getUri().getPath());
            tvImageSize.setText("Size: " + file.length() / 1024 + " KB");
        }

        btnDelete.setOnClickListener(v -> confirmDelete(file));
    }

    private void confirmDelete(DocumentFile file) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Do you really want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ImageDetailsActivity.this, GalleryActivity.class);
                        intent.putExtra("folderUri", folderUri.toString());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
