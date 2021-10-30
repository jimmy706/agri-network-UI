package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.plan.Needed;
import com.agrinetwork.helpers.CurrencyFormatter;

import java.util.List;

public class NeededFactorAdapter extends RecyclerView.Adapter<NeededFactorAdapter.ViewHolder> {

    private final Context context;
    private final List<Needed> neededFactors;

    public NeededFactorAdapter(Context context, List<Needed> neededFactors) {
        this.context = context;
        this.neededFactors = neededFactors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_needed_factor, parent, false);
        return new NeededFactorAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Needed needed = neededFactors.get(position);
        String priceFrom = CurrencyFormatter.format(needed.getPriceRange()[0]) + CurrencyFormatter.CURRENCY_SUFFIX;
        String priceTo = CurrencyFormatter.format(needed.getPriceRange()[1]) + CurrencyFormatter.CURRENCY_SUFFIX;
        String neededName = needed.getName();
        holder.name.setText(neededName);
        holder.priceRange.setText(priceFrom + " - " + priceTo);
    }

    @Override
    public int getItemCount() {
        return neededFactors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, priceRange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.needed_name);
            priceRange = itemView.findViewById(R.id.needed_price_range);
        }
    }
}
