package com.example.photoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridViewImages;
    private List<DocumentFile> imageFiles = new ArrayList<>();
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridViewImages = findViewById(R.id.gridViewImages);
        folderUri = Uri.parse(getIntent().getStringExtra("folderUri"));
        loadImages();

        gridViewImages.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            Intent intent = new Intent(GalleryActivity.this, ImageDetailsActivity.class);
            intent.putExtra("imageUri", imageFiles.get(position).getUri().toString());
            intent.putExtra("folderUri", folderUri.toString());
            startActivity(intent);
        });
    }

    private void loadImages() {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderUri);
        if (pickedDir != null && pickedDir.isDirectory()) {
            for (DocumentFile file : pickedDir.listFiles()) {
                if (file.getType() != null && file.getType().startsWith("image/")) {
                    imageFiles.add(file);
                }
            }
            gridViewImages.setAdapter(new ImageAdapter(this, imageFiles));
        }
    }
}
