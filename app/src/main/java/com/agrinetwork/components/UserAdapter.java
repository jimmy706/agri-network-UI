package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<User> users;
    private final Context context;
    private final UserService userService;
    private final String token;
    private final SharedPreferences sharedPreferences;
    private boolean moreActionMode = false;


    public UserAdapter (List<User> users, Context context) {
        this.users = users;
        this.context = context;
        this.userService = new UserService(context);

        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }

    public UserAdapter (List<User> users, Context context, boolean moreActionMode) {
        this.users = users;
        this.context = context;
        this.moreActionMode = moreActionMode;
        this.userService = new UserService(context);

        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_adapter, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);

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

        if(moreActionMode) {
               holder.followBtn.setVisibility(View.GONE);
               holder.moreActionBtn.setVisibility(View.VISIBLE);
        }
        else {
            holder.followBtn.setVisibility(View.VISIBLE);
            holder.moreActionBtn.setVisibility(View.GONE);

            boolean isFollowed = user.isFollowed();
            holder.followBtn.setChecked(isFollowed);
            changeTextColorBaseOnCheckedState(holder.followBtn, isFollowed);

            holder.followBtn.setOnClickListener(v -> {
                user.setFollowed(!user.isFollowed());
                if(isFollowed) {
                    unfollow(user.get_id());
                }
                else {
                    follow(user.get_id());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final TextView displayName, tag;
        private final ToggleButton followBtn;
        private final ImageButton moreActionBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            displayName = itemView.findViewById(R.id.display_name);
            tag = itemView.findViewById(R.id.user_tag);
            followBtn = itemView.findViewById(R.id.follow_btn);
            moreActionBtn = itemView.findViewById(R.id.more_action_btn);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void changeTextColorBaseOnCheckedState(ToggleButton toggleButton, boolean checked) {
        if(checked) {
            toggleButton.setTextColor(R.color.black);
        }
        else {
            toggleButton.setTextColor(R.color.white);
        }
    }

    private void follow(String targetUserId) {
        Call call = userService.follow(token, targetUserId);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    System.out.println("Followed");
                }
            }
        });
    }

    private void unfollow(String targetUserId) {
        Call call = userService.unfollow(token, targetUserId);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    System.out.println("Unfollowed");
                }
            }
        });
    }

    private void moveToUserWall(String userId) {
        Intent intent = new Intent(context, UserWallActivity.class);
        intent.putExtra("userId", userId);

        context.startActivity(intent);
    }
}
