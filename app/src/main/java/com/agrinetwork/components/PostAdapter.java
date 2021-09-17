package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.PostDetailActivity;
import com.agrinetwork.R;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.interfaces.ListItemClickListener;
import com.agrinetwork.service.PostService;
import com.google.android.material.card.MaterialCardView;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Variables.POST_DATE_FORMAT);
    private final List<PostItem> posts;
    private final Context context;
    private final String token;
    private final PostService postService;

    public PostAdapter(List<PostItem> posts, Context context) {
        this.posts = posts;
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        this.postService = new PostService(context);
    }



    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        @Setter
        private ListItemClickListener itemClickListener;

        private final ImageView avatar;
        private final TextView displayName, postTag, context, commentCount, reactionCount;
        private final ImageButton moreActionBtn;
        private final LinearLayout imageWrapper;
        private final ImageView commentBtn, reactionBtn, postImage;
        private final SliderView postImages;
        private final MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

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
            postImage = itemView.findViewById(R.id.image_view);
            postImages = itemView.findViewById(R.id.image_slider);
            cardView = itemView.findViewById(R.id.card_view);

        }

        @Override
        public void onClick(View view) {
            if(itemClickListener != null) {
                itemClickListener.onClick(view, getAdapterPosition(), false);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(itemClickListener != null) {
                itemClickListener.onClick(view, getAdapterPosition(), true);
            }
            return true;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_adapter, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem postItem = posts.get(position);

        holder.cardView.setOnClickListener(view -> {
            startPostDetailActivity(postItem);
        });

        holder.reactionBtn.setOnClickListener(view -> {
            if(postItem.isLiked()) {
                postItem.setLiked(false);
                postItem.setNumberOfReactions(postItem.getNumberOfReactions() - 1);
                holder.reactionBtn.setImageResource(R.drawable.ic_fav_border);
                unlike(postItem.get_id());
            }
            else {
                postItem.setLiked(true);
                postItem.setNumberOfReactions(postItem.getNumberOfReactions() + 1);
                holder.reactionBtn.setImageResource(R.drawable.ic_fav);
                like(postItem.get_id());
            }

            holder.reactionCount.setText(Integer.toString(postItem.getNumberOfReactions()));
        });

        holder.commentBtn.setOnClickListener(view -> {
            startPostDetailActivity(postItem);
        });

        String avatarUrl = postItem.getPostedBy().getAvatar();
        if(avatarUrl != null) {
            Picasso.get().load(avatarUrl).centerCrop().resize(40, 40).into(holder.avatar);
        }

        String name = postItem.getPostedBy().getFirstName() + " " + postItem.getPostedBy().getLastName();
        holder.displayName.setText(name);

        Date lastedModify = postItem.getLastModified();
        holder.postTag.setText(dateFormat.format(lastedModify));

        String content = postItem.getContent();
        holder.context.setText(content);

        List<String> images = postItem.getImages();
        if(!images.isEmpty()) {
            holder.imageWrapper.setVisibility(View.VISIBLE);
            Picasso picasso = Picasso.get();
                String postImageUrl = images.get(0);

                holder.postImages.setVisibility(View.GONE);
                picasso.load(postImageUrl).into(holder.postImage);
                holder.postImages.setVisibility(View.GONE);
        }
        else {
            holder.imageWrapper.setVisibility(View.GONE);
        }

        holder.commentCount.setText(Integer.toString(postItem.getNumberOfComments()));
        holder.reactionCount.setText(Integer.toString(postItem.getNumberOfReactions()));

        boolean isLiked = postItem.isLiked();
        if(isLiked) {
            holder.reactionBtn.setImageResource(R.drawable.ic_fav);
        }
        else {
            holder.reactionBtn.setImageResource(R.drawable.ic_fav_border);
        }

        // TODO: This code will be removed in future
        holder.commentCount.setVisibility(View.INVISIBLE);
        holder.reactionCount.setVisibility(View.INVISIBLE);
        holder.reactionBtn.setVisibility(View.GONE);
        holder.commentBtn.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void startPostDetailActivity(PostItem post) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(Variables.POST_ID_LABEL, post.get_id());

        context.startActivity(intent);
    }

    private void like(String postId) {
        Call call = postService.like(token, postId);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    System.out.println("Liked");
                }
            }
        });
    }

    private void unlike(String postId) {
        Call call = postService.unlike(token, postId);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    System.out.println("Unliked");
                }
            }
        });
    }

}
