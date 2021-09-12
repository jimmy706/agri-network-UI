package com.agrinetwork.components;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agrinetwork.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter<T> extends SliderViewAdapter<SliderAdapter.Holder> {

    List<T> images;

    public SliderAdapter(List<T> images) {
        this.images = images;
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_image_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder viewHolder, int position) {
        Object image = this.images.get(position);
        if(image instanceof Uri) {
            Uri imageUri = (Uri) this.images.get(position);
            viewHolder.imageView.setImageURI(imageUri);
        }
        else if(image instanceof Bitmap) {
            Bitmap imageBitmap = (Bitmap) this.images.get(position);
            viewHolder.imageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public int getCount() {
        return images.size();
    }

    public static class Holder extends SliderViewAdapter.ViewHolder {
        ImageView imageView;

        public Holder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
