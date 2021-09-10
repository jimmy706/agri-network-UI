package com.agrinetwork.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.PostItem;


import java.util.List;

import lombok.Getter;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<PostItem> posts;

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView avatar;
        private TextView displayName, postTag, context, commentCount, reactionCount;
        private ImageButton moreActionBtn;
        private LinearLayout imageWrapper;
        private ImageView commentBtn, reactionBtn;


        public ViewHolder(View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            postTag = itemView.findViewById(R.id.post_tag);
            context = itemView.findViewById(R.id.context);
            commentCount = itemView.findViewById(R.id.comment_count);
            reactionCount = itemView.findViewById(R.id.reaction_count);
            moreActionBtn = itemView.findViewById(R.id.more_action_btn);
            imageWrapper = itemView.findViewById(R.id.images_wrapper);
            commentBtn = itemView.findViewById(R.id.comment_button);
            reactionBtn = itemView.findViewById(R.id.reaction_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

}
