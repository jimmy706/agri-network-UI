package com.agrinetwork;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.agrinetwork.components.SliderAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostFormat;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.entities.PostTagItem;
import com.agrinetwork.service.MediaService;
import com.agrinetwork.service.PostService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.N)
public class CreatePostActivity extends AppCompatActivity {
    private final List<Uri> pickedImageUris = new ArrayList<>();
    private final List<String> pickedImageUrls = new ArrayList<>();

    private MediaService mediaService;
    private PostService postService;
    private String token;

    private final SliderAdapter<Uri> sliderAdapter = new SliderAdapter<>(pickedImageUris);
    private RelativeLayout pickedImageWrapper;
    private ProgressBar progressBar;
    private List<PostTagItem> pickedPostTags = new ArrayList<>();

    private List<String> requestTags = new ArrayList<>();
    private ChipGroup chipGroup;


    // Register for pick image intent
    ActivityResultLauncher<Intent> pickImageResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
       if(result.getResultCode() == Activity.RESULT_OK) {
           Intent data = result.getData();
           if(data.getClipData() != null) {
               // Choice multiple images

               ClipData clipData = data.getClipData();
               pickedImageUris.clear();
               for(int i = 0; i < clipData.getItemCount(); i++) {
                   Uri imageUri = clipData.getItemAt(i).getUri();
                   pickedImageUris.add(imageUri);
               }
               sliderAdapter.notifyDataSetChanged();
           }
           else if(data.getData() != null) {
               // Choice one

               Uri image = data.getData();
               pickedImageUris.clear();
               pickedImageUris.add(image);
               sliderAdapter.notifyDataSetChanged();
           }
           checkDisplayViewPickedImages();

       }
    });

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        postService = new PostService(this);
        mediaService = new MediaService(this);

        token = getToken();

        ImageButton pickMediaBtn = findViewById(R.id.pick_media_btn);
        progressBar = findViewById(R.id.progress_bar);
        pickMediaBtn.setOnClickListener(v -> {
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            pickImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                pickImageResultLauncher.launch(pickImageIntent);
        });

        SliderView sliderView = findViewById(R.id.image_slider);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.SLIDE);

        pickedImageWrapper = findViewById(R.id.picked_image_wrapper);
        checkDisplayViewPickedImages();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> {
            startActivity(new Intent(this, UserFeedActivity.class));
        });

        toolbar.setOnMenuItemClickListener(item -> {
            TextInputEditText contentInput = findViewById(R.id.input_content);
            if (isValid(contentInput)) {
                int id = item.getItemId();
                if (id == R.id.add_post_action) {

                    showLoading();

                    String content = contentInput.getText().toString();
                    String format = PostFormat.REGULAR.getLabel();

                    PostItem postItem = new PostItem();
                    postItem.setContent(content);
                    postItem.setImages(pickedImageUrls);
                    postItem.setTags(requestTags);

                    postItem.setFormat(format);

                    Call call = postService.addPost(token, postItem);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            CreatePostActivity.this.runOnUiThread(()-> {
                                closeLoading();
                                onCreatePostFailed();
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            CreatePostActivity.this.runOnUiThread(()-> {
                                closeLoading();
                                if(response.code() == 201) {
                                    onCreatePostSuccess();
                                }
                                else {
                                    onCreatePostFailed();
                                }
                            });

                        }
                    });

                }
            } else {
                Toast.makeText(this, "Vui l??ng nh???p n???i dung b??i vi???t", Toast.LENGTH_SHORT).show();
            }

            return false;
        });

        Button buttonAddTag = findViewById(R.id.btn_addTag);
        buttonAddTag.setOnClickListener(v->{
            DialogTagActivity dialogTagActivity = new DialogTagActivity(this);
            dialogTagActivity.show();
            dialogTagActivity.setSubmitListener(tags -> {

                chipGroup.removeAllViews();
                pickedPostTags.clear();
                pickedPostTags.addAll(tags);
                setTag(pickedPostTags);


            });

        });

        chipGroup = findViewById(R.id.chip_group);
    }

    // Only display image slider when there is/are image(s) picked
    public void checkDisplayViewPickedImages() {
        if(pickedImageWrapper != null) {
            if(!pickedImageUris.isEmpty()){
                pickedImageWrapper.setVisibility(View.VISIBLE);
                pickedImageUrls.clear();
                requestUploadImages();
            }
            else {
                pickedImageWrapper.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void requestUploadImages()  {
        final boolean[] uploadSuccess = {true};
        for(int i = 0; i < pickedImageUris.size(); i++) {
            Uri image = pickedImageUris.get(i);

            try {
                Call call = mediaService.uploadImage(image, token);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        uploadSuccess[0] = false;
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(response.code() == 201) {

                            String url = response.body().string();
                            pickedImageUrls.add(url);
                        }
                    }
                });
            } catch (Exception e) {
                uploadSuccess[0] = false;
                e.printStackTrace();
            }

            if(!uploadSuccess[0]) {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getToken() {
        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        return sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onCreatePostSuccess() {
        Toast.makeText(CreatePostActivity.this, "T???o b??i ????ng th??nh c??ng", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CreatePostActivity.this, UserFeedActivity.class));
    }

    private void onCreatePostFailed() {
        Toast.makeText(CreatePostActivity.this, "???? c?? l???i x???y ra", Toast.LENGTH_SHORT).show();
    }

    private void closeLoading() {
        progressBar.setVisibility(View.INVISIBLE);
    }


    private void setTag(final List<PostTagItem> tagList) {

        requestTags.clear();
        for (int index = 0; index < tagList.size(); index++) {

            final  PostTagItem chipTag = tagList.get(index);

            String chipName = chipTag.getName();

            requestTags.add(chipName);


            final Chip chip = new Chip(this);
            chip.setText(chipName);
            chip.setCloseIconResource(R.drawable.ic_delete);
            chip.setCloseIconEnabled(true);

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagList.remove(chipName);
                    chipGroup.removeView(chip);
                    tagList.clear();
                    requestTags.remove(chipName);
                }
            });

            chipGroup.addView(chip);
        }
    }

    private boolean isValid(TextInputEditText editText) {
        return !TextUtils.isEmpty(editText.getText());
    }
}