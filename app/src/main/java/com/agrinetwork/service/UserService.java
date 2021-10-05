package com.agrinetwork.service;

import android.content.Context;


import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UserService {
    private final Context context;
    private OkHttpClient client = new OkHttpClient();
    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/users";

    public UserService(Context context) {
        this.context = context;
    }

    public Call add(User user)  {
        RequestBody body = new FormBody.Builder()
                .add("firstName", user.getFirstName())
                .add("lastName", user.getLastName())
                .add("email", user.getEmail())
                .add("type", user.getType())
                .add("avatar", "")
                .add("phoneNumber", user.getPhoneNumber())
                .add("province", user.getProvince())
                .add("district", user.getDistrict())
                .add("ward", user.getWard())
                .build();

        Request request = new Request.Builder()
                .url(Variables.SERVICE_DOMAIN + "/users")
                .post(body)
                .build();

        return client.newCall(request);
    }

    public  Call getUserLogin(String token){
        Request request = new Request.Builder()
                .url(SERVICE_URL  )
                .header("Authorization", token)
                .get()
                .build();
        return client.newCall(request);
    }


    public Call getById(String token, String userId) {
        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL + "/" + userId)
                .get()
                .build();

        return client.newCall(request);
    }


    public Call update(User user, String token)  {
        RequestBody body = new FormBody.Builder()
                .add("firstName", user.getFirstName())
                .add("lastName", user.getLastName())
                .add("phoneNumber", user.getPhoneNumber())
                .add("province", user.getProvince())
                .add("avatar", user.getAvatar())
                .add("district", user.getDistrict())
                .add("ward", user.getWard())
                .build();

        Request request = new Request.Builder()
                .url(Variables.SERVICE_DOMAIN + "/users")
                .header("Authorization", token)
                .patch(body)
                .build();

        return client.newCall(request);
    }

    public Call follow(String token, String followTargetUser) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .post(body)
                .header("Authorization", token)
                .url(Variables.SERVICE_DOMAIN + "/users/" + followTargetUser + "/follow")
                .build();

        return client.newCall(request);
    }

    public Call unfollow(String token, String unfollowTargetUser) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .post(body)
                .url(Variables.SERVICE_DOMAIN + "/users/" + unfollowTargetUser + "/unfollow")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call getFollowings(String token, String userId) {
        Request request = new Request.Builder()
                .get()
                .header("Authorization", token)
                .url(Variables.SERVICE_DOMAIN + "/users/" + userId + "/followings")
                .build();

        return client.newCall(request);
    }

    public Call getFollowers(String token, String userId) {
        Request request = new Request.Builder()
                .get()
                .header("Authorization", token)
                .url(Variables.SERVICE_DOMAIN + "/users/" + userId + "/followers")
                .build();

        return client.newCall(request);
    }

    public Call searchByUser ( String searchText){
        Request request = new Request.Builder()
                .get()
                .url(SERVICE_URL +"/search"+"/userName?search="+searchText)
                .build();
        return client.newCall(request);

    }

    public Call sendFriendRequest(String token, String targetUserId) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .post(body)
                .url(Variables.SERVICE_DOMAIN + "/users/" + targetUserId + "/friend-request")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call approveFriendRequest(String token, String userWithFriendRequest) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .post(body)
                .url(Variables.SERVICE_DOMAIN + "/users/" + userWithFriendRequest + "/friend-request/approve")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call rejectFriendRequest(String token, String userWithFriendRequest) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .post(body)
                .url(Variables.SERVICE_DOMAIN + "/users/" + userWithFriendRequest + "/friend-request/reject")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call getFriendRequests(String token) {
        Request request = new Request.Builder()
                .get()
                .url(Variables.SERVICE_DOMAIN + "/users/friend-request")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call cancelFriendRequest(String token, String targetUserId) {
        Request request = new Request.Builder()
                .delete()
                .url(Variables.SERVICE_DOMAIN + "/users/" + targetUserId + "/friend-request")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call unfriend(String token, String targetUserId) {
        RequestBody body = new FormBody.Builder().build();

        Request request = new Request.Builder()
                .put(body)
                .url(Variables.SERVICE_DOMAIN + "/users/" + targetUserId + "/unfriend")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }

    public Call getFriends(String token, String userId) {
        Request request = new Request.Builder()
                .get()
                .url(Variables.SERVICE_DOMAIN + "/users/" + userId + "/friends")
                .header("Authorization", token)
                .build();

        return client.newCall(request);
    }
}
