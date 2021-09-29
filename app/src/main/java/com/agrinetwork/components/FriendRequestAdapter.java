package com.agrinetwork.components;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.FriendRequest;
import com.agrinetwork.entities.User;
import com.agrinetwork.enumeration.FriendRequestResponseStatus;
import com.agrinetwork.interfaces.HandleFriendRequestListener;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private final List<FriendRequest> friendRequests;
    private final Context context;
    private final UserService userService;
    private final String token;
    private final SharedPreferences sharedPreferences;
    private HandleFriendRequestListener handleFriendRequestListener;

    public FriendRequestAdapter(List<FriendRequest> friendRequests, Context context){
        this.friendRequests = friendRequests;
        this.context = context;
        this.userService = new UserService(context);
        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friend_request, parent, false);
        return new FriendRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest friendRequest = this.friendRequests.get(position);
        User requestUser = friendRequest.getFrom();

        String userAvatar = requestUser.getAvatar();
        if(userAvatar != null && !userAvatar.isEmpty()) {
            Picasso.get().load(userAvatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(holder.avatar);
        }
        holder.avatar.setOnClickListener(v -> {
            moveToUserWall(requestUser.get_id());
        });

        String fullName = requestUser.getFirstName() + " " + requestUser.getLastName();
        holder.displayName.setText(fullName);

        String userType = requestUser.getType();
        holder.tag.setText(userType);

        holder.rejectFriendRqBtn.setOnClickListener(v -> rejectFriendRequest(position));

        holder.acceptFriendRqBtn.setOnClickListener(v -> approveFriendRequest(position));
    }

    private void moveToUserWall(String userId) {
        Intent intent = new Intent(context, UserWallActivity.class);
        intent.putExtra("userId", userId);

        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView displayName, tag;
        private final MaterialButton acceptFriendRqBtn, rejectFriendRqBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            tag = itemView.findViewById(R.id.user_tag);
            acceptFriendRqBtn = itemView.findViewById(R.id.accept_friend_rq_btn);
            rejectFriendRqBtn = itemView.findViewById(R.id.reject_friend_rq_btn);
        }
    }

    private void approveFriendRequest(int position) {
        Call call = userService.approveFriendRequest(token, friendRequests.get(position).getFrom().get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    if(handleFriendRequestListener != null) {
                        handleFriendRequestListener.onFriendRequestResponse(position, FriendRequestResponseStatus.ACCEPT);
                    }
                }
            }
        });
    }

    private void rejectFriendRequest(int position) {
        Call call = userService.rejectFriendRequest(token, friendRequests.get(position).getFrom().get_id());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    if(handleFriendRequestListener != null) {
                        handleFriendRequestListener.onFriendRequestResponse(position, FriendRequestResponseStatus.REJECT);
                    }
                }
            }
        });
    }
}
