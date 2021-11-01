package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.PlanInfoActivity;
import com.agrinetwork.PostDetailActivity;
import com.agrinetwork.ProductDetailActivity;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.R;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostFormat;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.helpers.AttributesConverter;
import com.agrinetwork.helpers.AttributesToPlanConverter;
import com.agrinetwork.interfaces.listeners.DeletePostListener;
import com.agrinetwork.interfaces.ListItemClickListener;
import com.agrinetwork.service.PostService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;


import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final SharedPreferences sharedPreferences;
    private final String currentLoginUserId;
    private final Picasso picasso = Picasso.get();
    
    @Setter
    private DeletePostListener deletePostListener;

    public PostAdapter(List<PostItem> posts, Context context) {
        this.posts = posts;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
        currentLoginUserId = sharedPreferences.getString(Variables.CURRENT_LOGIN_USER_ID, "");
        this.postService = new PostService(context);
    }

    @Getter
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        
        @Setter
        private ListItemClickListener itemClickListener;

        private ImageView avatar;
        private TextView displayName, postTag, context, commentCount, reactionCount, productName, productPrice, planName, planDueDate;
        private ImageButton moreActionBtn;
        private LinearLayout imageWrapper, productRefWrapper, planWrapper;
        private ImageView commentBtn, reactionBtn, postImage, productThumbnail;
        private SliderView postImages;
        private MaterialCardView cardView;
        private ChipGroup chipGroup;
        private RecyclerView planDetailList;
        private Button viewMorePlanBtn;

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
            chipGroup = itemView.findViewById(R.id.chip_group_feed);
            productRefWrapper = itemView.findViewById(R.id.product_ref_wrapper);
            productThumbnail = itemView.findViewById(R.id.product_thumbnail);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            planWrapper = itemView.findViewById(R.id.plan_wrapper);
            planName = itemView.findViewById(R.id.plan_name);
            planDueDate = itemView.findViewById(R.id.plan_duedate);
            planDetailList = itemView.findViewById(R.id.plan_detail_list);
            viewMorePlanBtn = itemView.findViewById(R.id.watch_more_plan);
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
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            picasso.load(avatarUrl).centerCrop().resize(40, 40).into(holder.avatar);
        }
        holder.avatar.setOnClickListener(v -> {
            String userId = postItem.getPostedBy().get_id();
            Intent intent = new Intent(context, UserWallActivity.class);
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });

        String name = postItem.getPostedBy().getFirstName() + " " + postItem.getPostedBy().getLastName();
        holder.displayName.setText(name);

        Date lastedModify = postItem.getLastModified();
        holder.postTag.setText(dateFormat.format(lastedModify));

        String content = postItem.getContent();
        holder.context.setText(Html.fromHtml(content));

        List<String> images = postItem.getImages();
        if(!images.isEmpty()) {
            holder.imageWrapper.setVisibility(View.VISIBLE);
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

        if(!currentLoginUserId.equals(postItem.getPostedBy().get_id())) {
            holder.moreActionBtn.setVisibility(View.GONE);
        }
        else {
            holder.moreActionBtn.setOnClickListener(v -> {
                showPostActions(postItem);
            });
        }

        if(!postItem.getTags().isEmpty()) {
            holder.chipGroup.removeAllViews();
            for(int i = 0; i < postItem.getTags().size(); i++){
                String chipName = postItem.getTags().get(i);
                final Chip chip = new Chip(context);
                chip.setText(chipName);
                holder.chipGroup.addView(chip);
            }
        }

        if(postItem.getFormat().equals(PostFormat.SELL.getLabel())) {
            displayProductInPost(postItem, holder);
        }
        else if (postItem.getFormat().equals(PostFormat.PLAN.getLabel())) {
            displayPlanInPost(postItem, holder);
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayPlanInPost(PostItem postItem, ViewHolder viewHolder) {
        Map<String, String> attributes = new AttributesConverter(postItem.getAttributes()).toMap();
        viewHolder.planWrapper.setVisibility(View.VISIBLE);
        Plan plan = new AttributesToPlanConverter(attributes).toPlan();
        System.out.println(plan);

        SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
        viewHolder.planName.setText(plan.getName());
        Date fromDate = plan.getFrom();
        Date toDate = plan.getTo();
        viewHolder.planDueDate.setText(sdf.format(fromDate) + " - " + sdf.format(toDate));
        List<PlanDetail> planDetails = new ArrayList<>();
        int steps = plan.getPlantDetails().size();
        if (steps > 2) {
            viewHolder.viewMorePlanBtn.setVisibility(View.VISIBLE);
            viewHolder.viewMorePlanBtn.setText("Xem thêm (" + (steps - 2) + "+)");
            viewHolder.viewMorePlanBtn.setOnClickListener(v -> startPostDetailActivity(postItem));
        }
        else {
            viewHolder.viewMorePlanBtn.setVisibility(View.GONE);
        }

        for(int i = 0; i < 2; i++) {
            PlanDetail planDetail = plan.getPlantDetails().get(i);
            if(planDetail != null) {
                planDetails.add(planDetail);
            }
        }
        viewHolder.planDetailList.setAdapter(new PlanDetailAdapter(context, planDetails));
        viewHolder.planDetailList.setLayoutManager(new LinearLayoutManager(context));
    }

    private void displayProductInPost(PostItem postItem, ViewHolder holder) {
        Map<String, String> attributes = new AttributesConverter(postItem.getAttributes()).toMap();
        holder.productRefWrapper.setVisibility(View.VISIBLE);
        if(attributes.containsKey("thumbnail")) {
            picasso
                    .load(attributes.get("thumbnail"))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.productThumbnail);
        }
        holder.productName.setText(attributes.get("name"));
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        numberFormat.setCurrency(Currency.getInstance("VND"));
        numberFormat.setMaximumFractionDigits(0);

        if(attributes.get("price")!= null){
            String price = attributes.get("price").trim();
            holder.productPrice.setText(numberFormat.format(Double.parseDouble(price)));
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void startPostDetailActivity(PostItem post) {
        String format = post.getFormat();
        Intent intent = null;
        if(format.equals(PostFormat.REGULAR.getLabel())) {
            intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(Variables.POST_ID_LABEL, post.get_id());
        }
        else if(format.equals(PostFormat.SELL.getLabel())) {
            intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", post.getRef());
        } else if(format.equals(PostFormat.PLAN.getLabel())) {
            intent = new Intent(context, PlanInfoActivity.class);
            intent.putExtra("planId", post.getRef());
        }

        if(intent != null) {
            context.startActivity(intent);
        }
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

    private void showPostActions(PostItem postItem) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.post_action_bottom_sheet_popup);

        RelativeLayout editPost = bottomSheetDialog.findViewById(R.id.edit_post);
        editPost.setOnClickListener(v -> {
            Toast.makeText(context, "Mở edit post", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        RelativeLayout deletePost = bottomSheetDialog.findViewById(R.id.delete_post);
        deletePost.setOnClickListener(v -> {
            if(deletePostListener != null) {
                deletePostListener.onDelete(postItem.get_id());
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public long getItemId(int position) {
        return posts.get(position).getLastModified().getTime();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
