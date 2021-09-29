package com.agrinetwork;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.components.PostTagAdapter;
import com.agrinetwork.entities.PostTagItem;
import com.agrinetwork.service.TagService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DialogTagActivity extends Dialog implements View.OnClickListener{
    private Activity activity;
    private Dialog dialog;
    private Button cancel, add;

    private TagService tagService;
    private RecyclerView recyclerView;
    private PostTagAdapter postTagAdapter;
    private List<PostTagItem> postTagItemList = new ArrayList<>();


    public DialogTagActivity(Activity activity){
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dialog_tag);
        cancel =  findViewById(R.id.btn_cancel);
        add = findViewById(R.id.btn_add);
        cancel.setOnClickListener(this);
        add.setOnClickListener(this);

        tagService = new TagService(activity);
            recyclerView = findViewById(R.id.post_tag_list);
            postTagAdapter = new PostTagAdapter(postTagItemList,activity);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            recyclerView.setAdapter(postTagAdapter);
            recyclerView.setLayoutManager(linearLayoutManager);

            fetchPostTag();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                activity.finish();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void fetchPostTag(){
        Call getAllTag = tagService.getPostTag();
        getAllTag.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getPostTag, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call getPostTag, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    Gson gson = new Gson();
                    String responseListTag = response.body().string();

                    Type tagListType  = new TypeToken<List<PostTagItem>>(){}.getType();
                    List<PostTagItem> tagItemResponse = gson.fromJson(responseListTag,tagListType);

                    activity.runOnUiThread(()->{
                        postTagItemList.addAll(tagItemResponse);
                        postTagAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}

