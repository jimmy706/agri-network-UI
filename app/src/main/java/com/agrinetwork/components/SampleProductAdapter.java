package com.agrinetwork.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.product.SampleProduct;
import com.agrinetwork.interfaces.listeners.SampleProductClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import lombok.Setter;

public class SampleProductAdapter extends RecyclerView.Adapter<SampleProductAdapter.ViewHolder> {

    private Context context;
    private List<SampleProduct> sampleProducts;
    private final Picasso picasso = Picasso.get();
    @Setter
    private SampleProductClickListener clickListener;

    public SampleProductAdapter(Context context, List<SampleProduct> sampleProducts) {
        this.context = context;
        this.sampleProducts = sampleProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_sample_product, parent, false);
        return new SampleProductAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SampleProduct sampleProduct = sampleProducts.get(position);
        List<String> thumbnails = sampleProduct.getThumbnails();
        if(thumbnails != null && !thumbnails.isEmpty()) {
            picasso.load(thumbnails.get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.thumbnail);
        }

        String productName = sampleProduct.getName();
        holder.name.setText(productName);

        holder.cardView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onClick(sampleProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleProducts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;
        private final CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thumbnail = itemView.findViewById(R.id.product_thumbnail);
            this.name = itemView.findViewById(R.id.product_name);
            this.cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
