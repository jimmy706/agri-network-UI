package com.agrinetwork.components;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.R;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.squareup.picasso.Picasso;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.text.BreakIterator;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private final List<User> users;
    private final Context context;
    private final UserService userService;



    public SearchAdapter (List<User> users, Context context){
        this.users = users;
        this.context = context;
        this.userService = new UserService(context);


        }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
      return new SearchAdapter.ViewHolder(resultView);
       // return  new ViewHolder(resultView);
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


        String fullName = user.getFirstName() + " " + user.getLastName();
        holder.displayName.setText(fullName);

        String province = user.getProvince();
        holder.displayProvince.setText(province);

        String userType = user.getType();
        holder.displayType.setText(userType);


    }



    @Override
    public int getItemCount() {
        return users.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatar;
        private final  TextView displayName ,displayProvince,displayType;
        private final  Button  buttonDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar_user);
            displayName = itemView.findViewById(R.id.fullName);
            displayProvince = itemView.findViewById(R.id.province);
            displayType = itemView.findViewById(R.id.user_type);
            buttonDetail = itemView.findViewById(R.id.btn_detail);



//           buttonDetail.setOnClickListener(new View.OnClickListener() {
//               @Override
//               public void onClick(View view) {
//
//               }
//           });
        }
    }
}
