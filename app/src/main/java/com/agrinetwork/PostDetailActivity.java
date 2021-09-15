package com.agrinetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.databinding.ActivityPostDetailBinding;
import com.agrinetwork.entities.Post;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.PostService;
import com.google.android.material.appbar.MaterialToolbar;
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
    private String postId;
    private String token;
    private ProgressBar progressBar;
    private View postContentContainer;
    private ImageView userAvatar, imageView;
    private TextView userDisplayName, postTag, context, commentCount, reactionCount;;
    private View imagesWrapper;
    private SliderView imageSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postService = new PostService(this);

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

        fetchPost();

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
                Post post = gson.fromJson(jsonData, Post.class);

                PostDetailActivity.this.runOnUiThread(()-> {
                    renderData(post);
                });
            }
        });
    }

    private void getJwtFromSharedPreference() {
        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
    }

    private void renderData(Post post) {
        progressBar.setVisibility(View.GONE);
        postContentContainer.setVisibility(View.VISIBLE);

        User postedByUser = post.getPostedBy();
        String avatarUrl = postedByUser.getAvatar();
        if(avatarUrl != null) {
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
            }
            else {
                List<Bitmap> imageBitmaps = new ArrayList<>();
                SliderAdapter<Bitmap> sliderAdapter = new SliderAdapter<>(imageBitmaps);
                imageSlider.setSliderAdapter(sliderAdapter);
                postImages.forEach(img -> {
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
            imagesWrapper.setVisibility(View.GONE);
        }

        String content = post.getContent();
        context.setText(content);

        String numberOfComments = Integer.toString(post.getNumberOfComments());
        String numberOfReactions = Integer.toString(post.getNumberOfReactions());

        commentCount.setText(numberOfComments);
        reactionCount.setText(numberOfReactions);
    }
}