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
import com.agrinetwork.entities.RecommendUser;
import com.agrinetwork.service.UserService;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecommendUserAdapter extends RecyclerView.Adapter<RecommendUserAdapter.ViewHolder>{
    private final Context context;
    private final List<RecommendUser> users;
    private final UserService userService;
    private final String token;
    private final SharedPreferences sharedPreferences;

    public RecommendUserAdapter(List<RecommendUser> users, Context context) {
        this.users = users;
        this.context = context;
        this.userService = new UserService(context);
        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_user_item, parent, false);
        return new RecommendUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendUser user = users.get(position);

        String userAvatar = user.getAvatar();
        if(userAvatar != null && !userAvatar.isEmpty()) {
            Picasso.get().load(userAvatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(holder.avatar);
        }
        holder.avatar.setOnClickListener(v -> {
            moveToUserWall(user.get_id());
        });

        String fullName = user.getFirstName() + " " + user.getLastName();
        holder.displayName.setText(fullName);

        String userType = user.getType();
        holder.tag.setText(userType);

        boolean isPendingFriendRq = user.isPendingFriendRequest();
        if(isPendingFriendRq) {
            holder.addFriendBtn.setVisibility(View.GONE);
            holder.deleteFriendRqBtn.setVisibility(View.VISIBLE);
        }
        else {
            holder.addFriendBtn.setVisibility(View.VISIBLE);
            holder.deleteFriendRqBtn.setVisibility(View.GONE);
        }

        holder.addFriendBtn.setOnClickListener(v -> {
            sendFriendRequest(user.get_id());
            user.setPendingFriendRequest(true);

            holder.addFriendBtn.setVisibility(View.GONE);
            holder.deleteFriendRqBtn.setVisibility(View.VISIBLE);
        });
        holder.deleteFriendRqBtn.setOnClickListener(v -> {
            cancelFriendRequest(user.get_id());
            user.setPendingFriendRequest(false);

            holder.addFriendBtn.setVisibility(View.VISIBLE);
            holder.deleteFriendRqBtn.setVisibility(View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView displayName, tag;
        private final MaterialButton addFriendBtn, deleteFriendRqBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            tag = itemView.findViewById(R.id.user_tag);
            addFriendBtn = itemView.findViewById(R.id.add_friend_btn);
            deleteFriendRqBtn = itemView.findViewById(R.id.delete_friend_rq_btn);
        }
    }

    private void moveToUserWall(String userId) {
        Intent intent = new Intent(context, UserWallActivity.class);
        intent.putExtra("userId", userId);

        context.startActivity(intent);
    }

    private void sendFriendRequest(String targetUser) {
        Call call = userService.sendFriendRequest(token, targetUser);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

    private void cancelFriendRequest(String targetUser) {
        Call call = userService.cancelFriendRequest(token, targetUser);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                
            }
        });
    }
}
