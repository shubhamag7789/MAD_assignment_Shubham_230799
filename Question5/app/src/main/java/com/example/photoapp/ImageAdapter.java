package com.example.photoapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.documentfile.provider.DocumentFile;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<DocumentFile> imageFiles;

    public ImageAdapter(Context context, List<DocumentFile> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @Override
    public int getCount() {
        return imageFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Use Glide to load images sharply into the ImageView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        int size = parent.getWidth() / 3;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        Glide.with(context)
                .load(imageFiles.get(position).getUri())
                .centerCrop()
                .into(imageView);
        return imageView;
    }
}
