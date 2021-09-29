package com.agrinetwork.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.PostTagItem;
import com.agrinetwork.service.TagService;

import java.util.List;

public class PostTagAdapter extends RecyclerView.Adapter<PostTagAdapter.ViewHolder>{
    private final List<PostTagItem> postTagItemsList;
    private final Context context;
    private final TagService tagService;

    public PostTagAdapter(List<PostTagItem> postTagItemList, Context context){
        this.postTagItemsList = postTagItemList;
        this.context = context;
        this.tagService = new TagService(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        return new PostTagAdapter.ViewHolder(resultView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PostTagItem postTagItem = postTagItemsList.get(position);

        String tagName = postTagItem.getName();
        holder.tag.setText(tagName);


    }

    @Override
    public int getItemCount() {
        return postTagItemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tag;
        private  final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tag = itemView.findViewById(R.id.tag_item);
            checkBox = itemView.findViewById(R.id.checkbox_id);
        }
    }
}
