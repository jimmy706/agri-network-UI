package com.agrinetwork.components;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.agrinetwork.R;
import com.agrinetwork.entities.PostTagItem;
import com.google.android.material.chip.Chip;
import java.util.List;

public class TagPickedAdapter extends RecyclerView.Adapter<TagPickedAdapter.ViewHolder>{
    private final List<PostTagItem> pickedPostTags;
    private final Context context;

    public TagPickedAdapter(List<PostTagItem> pickedPostTags, Context context){
        this.pickedPostTags = pickedPostTags;
        this.context = context;

    }

    @NonNull
    @Override
    public TagPickedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultTag = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_picked, parent, false);
        return new TagPickedAdapter.ViewHolder(resultTag);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  PostTagItem chipTag = pickedPostTags.get(position);
        String chipName = chipTag.getName();
        holder.tag.setText(chipName);

    }

    @Override
    public int getItemCount() {
        return pickedPostTags.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private  final Chip tag;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tag = itemView.findViewById(R.id.chip_item);

        }
    }
}
