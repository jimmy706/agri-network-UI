package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.agrinetwork.R;
import com.agrinetwork.entities.PostItem;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import lombok.Getter;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private static final String POST_DATE_FORMAT = "dd-MMM-yyyy hh:mm";
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(POST_DATE_FORMAT);
    private final List<PostItem> posts;
    private final Context context;

    public PostAdapter(List<PostItem> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView avatar;
        private final TextView displayName, postTag, context, commentCount, reactionCount;
        private final ImageButton moreActionBtn;
        private final LinearLayout imageWrapper;
        private final ImageView commentBtn, reactionBtn, postImage;
        private final SliderView postImages;


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
            postImage = itemView.findViewById(R.id.image_view);
            postImages = itemView.findViewById(R.id.image_slider);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_adapter, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem postItem = posts.get(position);

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
            if(images.size() == 1) {
                String postImageUrl = images.get(0);

                holder.postImages.setVisibility(View.GONE);
                picasso.load(postImageUrl).into(holder.postImage);
            }
            else {
                List<Bitmap> imageBitmaps = new ArrayList<>();
                holder.postImage.setVisibility(View.GONE);
                SliderAdapter<Bitmap> sliderAdapter = new SliderAdapter<>(imageBitmaps);
                holder.postImages.setSliderAdapter(sliderAdapter);

                images.forEach(img -> {
                    System.out.println("Loading image from: " + img);
                    picasso.load(img).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            System.out.println("Image added");
                            imageBitmaps.add(bitmap);
                            sliderAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            e.printStackTrace();
                            System.out.println("Failed to load image: " + e.getMessage());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });

                });
            }
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
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

}
