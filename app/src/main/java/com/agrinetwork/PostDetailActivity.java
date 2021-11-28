package com.agrinetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.agrinetwork.components.CommentAdapter;
import com.agrinetwork.components.PlanDetailAdapter;
import com.agrinetwork.components.PostAdapter;
import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Comment;
import com.agrinetwork.entities.Post;
import com.agrinetwork.entities.PostFormat;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.helpers.AttributesConverter;
import com.agrinetwork.helpers.AttributesToPlanConverter;
import com.agrinetwork.service.PostService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PostDetailActivity extends AppCompatActivity {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Variables.POST_DATE_FORMAT, new Locale("vi", "VI"));
    private PostService postService;

    private final List<Comment> comments = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String postId;
    private Post post;
    private String token;

    private ProgressBar progressBar;
    private View postContentContainer;
    private ImageView userAvatar, imageView, productThumbnail;
    private ImageView reactionBtn;
    private TextView userDisplayName, postTag, context, commentCount, reactionCount, noCommentMessage, planName, planDueDate, productName, productPrice;
    private View imagesWrapper;
    private SliderView imageSlider;
    private ImageButton sendCommentBtn;
    private TextInputEditText commentInput;
    private RecyclerView commentsRecyclerView, planDetailList;
    private ChipGroup chipGroup;
    private List<String> tags = new ArrayList<>();
    private LinearLayout planWrapper, productRefWrapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postService = new PostService(this);
        commentAdapter = new CommentAdapter(this, comments);

        Intent intent = getIntent();
        postId = intent.getExtras().getString(Variables.POST_ID_LABEL);
        getJwtFromSharedPreference();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            startActivity(new Intent(this, UserFeedActivity.class));
        });

        progressBar = findViewById(R.id.progress_bar);
        postContentContainer = findViewById(R.id.post_content_container);
        userAvatar = findViewById(R.id.avatar);
        userDisplayName = findViewById(R.id.display_name);
        postTag = findViewById(R.id.post_tag);
        imagesWrapper = findViewById(R.id.images_wrapper);
        imageView = findViewById(R.id.image_view);
        imageSlider = findViewById(R.id.image_slider);
        context = findViewById(R.id.context);
        commentCount = findViewById(R.id.comment_count);
        reactionCount = findViewById(R.id.reaction_count);
        sendCommentBtn = findViewById(R.id.submit_comment);
        commentInput = findViewById(R.id.comment_input);
        commentsRecyclerView = findViewById(R.id.comment_list);
        noCommentMessage = findViewById(R.id.no_comment_message);
        reactionBtn = findViewById(R.id.reaction_button);
        planWrapper = findViewById(R.id.plan_wrapper);
        productRefWrapper = findViewById(R.id.product_ref_wrapper);
        planDetailList = findViewById(R.id.plan_detail_list);
        planDueDate = findViewById(R.id.plan_duedate);
        planName = findViewById(R.id.plan_name);
        productThumbnail = findViewById(R.id.product_thumbnail);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setAdapter(commentAdapter);
        chipGroup = findViewById(R.id.chip_group_feed);

        fetchPost();
        handleEvents();


    }

    private void fetchPost() {
        progressBar.setVisibility(View.VISIBLE);
        postContentContainer.setVisibility(View.GONE);

        Call call = postService.getPostDetail(token, postId);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Gson gson = new Gson();
                String jsonData = response.body().string();
                post = gson.fromJson(jsonData, Post.class);
                PostDetailActivity.this.runOnUiThread(()-> {
                    renderData();
                    renderComments();
                    renderTags();
                });
            }
        });
    }

    private void getJwtFromSharedPreference() {
        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
    }

    private void renderData() {
        progressBar.setVisibility(View.GONE);
        postContentContainer.setVisibility(View.VISIBLE);

        User postedByUser = post.getPostedBy();
        String avatarUrl = postedByUser.getAvatar();
        if(avatarUrl != null && !avatarUrl.isEmpty()) {
            Picasso.get().load(avatarUrl).into(userAvatar);
        }

        String fullName = postedByUser.getFirstName() + " " + postedByUser.getLastName();
        userDisplayName.setText(fullName);

        String createPostDate = dateFormat.format(post.getCreatedDate());
        postTag.setText(createPostDate);

        List<String> postImages = post.getImages();
        if(!postImages.isEmpty()) {
            Picasso picasso = Picasso.get();
            if(postImages.size() == 1) {
                picasso.load(postImages.get(0)).into(imageView);
                imageSlider.setVisibility(View.GONE);
            }
            else {
                List<Bitmap> imageBitmaps = new ArrayList<>();
                SliderAdapter<Bitmap> sliderAdapter = new SliderAdapter<>(imageBitmaps);
                imageSlider.setSliderAdapter(sliderAdapter);
                postImages.forEach(img -> {
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
            imagesWrapper.setVisibility(View.GONE);
        }

        String content = post.getContent();
        context.setText(Html.fromHtml(content));

        String numberOfComments = Integer.toString(post.getNumberOfComments());
        String numberOfReactions = Integer.toString(post.getNumberOfReactions());

        commentCount.setText(numberOfComments);
        reactionCount.setText(numberOfReactions);

        if(post.isLiked()) {
            reactionBtn.setImageResource(R.drawable.ic_fav);
        }
        else {
            reactionBtn.setImageResource(R.drawable.ic_fav_border);
        }

        if (post.getFormat().equals(PostFormat.PLAN.getLabel())) {
            displayPlanInPost();
        } else if (post.getFormat().equals(PostFormat.SELL.getLabel())) {
            displayProductInPost();
        }
    }

    private void renderComments() {
        if(!post.getComments().isEmpty()) {
            comments.addAll(post.getComments());
            commentAdapter.notifyDataSetChanged();

            noCommentMessage.setVisibility(View.GONE);
        }
        else {
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentMessage.setVisibility(View.VISIBLE);
        }
    }

    private void handleEvents() {
        sendCommentBtn.setOnClickListener(v -> {
            String commentContent = commentInput.getText().toString();

            Call call = postService.addComment(token, postId, commentContent);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.code() == 200) {
                        Gson gson = new Gson();
                        Comment newComment = gson.fromJson(response.body().string(), Comment.class);

                        PostDetailActivity.this.runOnUiThread(() -> {
                            commentInput.setText("");

                            // Hide keyboard after done
                            InputMethodManager im = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            View view = getCurrentFocus();
                            if(view != null) {
                                im.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            commentInput.clearFocus();

                            comments.add(newComment);
                            post.setNumberOfComments(post.getNumberOfComments() + 1);
                            post.getComments().add(newComment);
                            commentAdapter.notifyItemRangeInserted(comments.size() - 1, comments.size());
                            renderData();

                            noCommentMessage.setVisibility(View.GONE);
                            commentsRecyclerView.setVisibility(View.VISIBLE);
                        });
                    }
                    else {
                        PostDetailActivity.this.runOnUiThread(()-> {
                            Toast.makeText(PostDetailActivity.this, "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });

        reactionBtn.setOnClickListener(v -> {
            if(post != null) {
                System.out.println(post.isLiked());
                if(post.isLiked()) {
                    // Unlike
                    post.setNumberOfReactions(post.getNumberOfReactions() - 1);
                    unlike();
                }
                else {
                    // Like
                    post.setNumberOfReactions(post.getNumberOfReactions() + 1);
                    like();
                }
                post.setLiked(!post.isLiked());
                renderData();
            }
        });

    }

    private void like() {
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

    private void unlike() {
        Call call = postService.unlike(token, postId);
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

    private void renderTags() {
        if(!post.getTags().isEmpty()) {
            tags.addAll(post.getTags());

            for(int i = 0; i <tags.size(); i++){
                String tagName = tags.get(i);

                final Chip chip = new Chip(this);
                chip.setText(tagName);
                chipGroup.addView(chip);
            }

        }

    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayPlanInPost() {
        Map<String, String> attributes = new AttributesConverter(post.getAttributes()).toMap();
        planWrapper.setVisibility(View.VISIBLE);
        Plan plan = new AttributesToPlanConverter(attributes).toPlan();

        SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
        planName.setText(plan.getName());
        Date fromDate = plan.getFrom();
        Date toDate = plan.getTo();
        planDueDate.setText(sdf.format(fromDate) + " - " + sdf.format(toDate));
        List<PlanDetail> planDetails = new ArrayList<>();
        int steps = plan.getPlantDetails().size();

        for(int i = 0; i < steps; i++) {
            PlanDetail planDetail = plan.getPlantDetails().get(i);
            if(planDetail != null) {
                planDetails.add(planDetail);
            }
        }

        planDetailList.setAdapter(new PlanDetailAdapter(this, planDetails));
        planDetailList.setLayoutManager(new LinearLayoutManager(this));

        planWrapper.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanInfoActivity.class);
            intent.putExtra("planId", post.getRef());
            startActivity(intent);
        });
    }

    private void displayProductInPost() {
        Map<String, String> attributes = new AttributesConverter(post.getAttributes()).toMap();
        productRefWrapper.setVisibility(View.VISIBLE);
        if(attributes.containsKey("thumbnail")) {
            Picasso.get()
                    .load(attributes.get("thumbnail"))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(productThumbnail);
        }
        productName.setText(attributes.get("name"));
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        numberFormat.setCurrency(Currency.getInstance("VND"));
        numberFormat.setMaximumFractionDigits(0);

        if(attributes.get("price")!= null){
            double price = Double.parseDouble(attributes.get("price").trim());
            if (price > 0) {
                productPrice.setText(numberFormat.format(price));
            } else {
                productPrice.setVisibility(View.GONE);
            }
        }
        productRefWrapper.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("productId", post.getRef());
            startActivity(intent);
        });
    }

}