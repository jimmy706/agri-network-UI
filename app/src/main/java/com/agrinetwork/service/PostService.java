package com.agrinetwork.service;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.helpers.UrlHelper;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PostService {
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/posts";

    public PostService(Context context) {
        this.context = context;
    }

    public Call addPost(String token, PostItem postItem) {
        Gson gson = new Gson();
        String json = gson.toJson(postItem);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SERVICE_URL)
                .header("Authorization", token)
                .post(body)
                .build();

        return client.newCall(request);
    }

    public Call getPosts(String token, int page, int limit) {
        Map<String, Integer> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", limit);
        UrlHelper<Integer> urlHelper = new UrlHelper<>();

        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL +"?" + urlHelper.convertFromMapToQueryString(params))
                .get()
                .build();

        return client.newCall(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Call getPostsFromUser(String token, String uid, Integer page, Integer limit) {
        UrlHelper<Integer> urlHelper = new UrlHelper<>();

        Map<String, Integer> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", limit);

        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL + "/owner/" + uid + "?" + urlHelper.convertFromMapToQueryString(params))
                .get()
                .build();

        return client.newCall(request);
    }

    public Call getCommentsAndReactionsCountFromPost(String postId){
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/" + postId + "/commentsAndReactionsCount")
                .get()
                .build();

        return client.newCall(request);
    }

    public Call getPostDetail(String postId) {
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/" + postId)
                .get()
                .build();

        return client.newCall(request);
    }

    public Call deletePost(String postId) {
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/" + postId)
                .delete()
                .build();

        return client.newCall(request);
    }
}
