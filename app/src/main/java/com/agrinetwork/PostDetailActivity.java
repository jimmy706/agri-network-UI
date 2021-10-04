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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.agrinetwork.components.CommentAdapter;
import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Comment;
import com.agrinetwork.entities.Post;
import com.agrinetwork.entities.User;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PostDetailActivity extends AppCompatActivity {
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Variables.POST_DATE_FORMAT);
    private PostService postService;

    private final List<Comment> comments = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String postId;
    private Post post;
    private String token;

    private ProgressBar progressBar;
    private View postContentContainer;
    private ImageView userAvatar, imageView;
    private ImageButton reactionBtn;
    private TextView userDisplayName, postTag, context, commentCount, reactionCount, noCommentMessage;
    private View imagesWrapper;
    private SliderView imageSlider;
    private ImageButton sendCommentBtn;
    private TextInputEditText commentInput;
    private RecyclerView commentsRecyclerView;
    private ChipGroup chipGroup;
    private List<String> tags = new ArrayList<>();


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
        userAvatar = findViewById(R.id.user_avatar);
        userDisplayName = findViewById(R.id.user_name);
        postTag = findViewById(R.id.post_tag);
        imagesWrapper = findViewById(R.id.images_wrapper);
        imageView = findViewById(R.id.image_view);
        imageSlider = findViewById(R.id.image_slider);
        context = findViewById(R.id.post_context);
        commentCount = findViewById(R.id.comment_count);
        reactionCount = findViewById(R.id.reaction_count);
        sendCommentBtn = findViewById(R.id.submit_comment);
        commentInput = findViewById(R.id.comment_input);
        commentsRecyclerView = findViewById(R.id.comment_list);
        noCommentMessage = findViewById(R.id.no_comment_message);
        reactionBtn = findViewById(R.id.reaction_button);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.setAdapter(commentAdapter);
        chipGroup = findViewById(R.id.tag_group);

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
        context.setText(content);

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
}