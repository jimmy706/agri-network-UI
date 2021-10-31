package com.agrinetwork.components;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.RecommendUserDemand;
import com.agrinetwork.service.RecommendService;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecommendUserDemandAdapter extends RecyclerView.Adapter<RecommendUserDemandAdapter.ViewHolder> {
    private final Context context;
    private final List<RecommendUserDemand> userDemandList;
    private final RecommendService recommendService;
    private final String token;
    private final SharedPreferences sharedPreferences;

    public RecommendUserDemandAdapter(List<RecommendUserDemand> userDemandList, Context context){
        this.context = context;
        this.userDemandList = userDemandList;
        this.recommendService = new RecommendService(context);
        this.sharedPreferences = context.getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        this.token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_recommend_demand, parent, false);
        return new RecommendUserDemandAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendUserDemandAdapter.ViewHolder holder, int position) {
        RecommendUserDemand userDemand = userDemandList.get(position);

        String userAvatar = userDemand.getAvatar();
        if(userAvatar != null & !userAvatar.isEmpty()){
            Picasso.get().load(userAvatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(holder.avatar);
        }
        holder.avatar.setOnClickListener(v->{
            Intent intent = new Intent(context, UserWallActivity.class);
            intent.putExtra("userId", userDemand.get_id());
            context.startActivity(intent);
        });

        String fullName = userDemand.getFirstName() + " " + userDemand.getLastName();
        holder.textName.setText(fullName);

        String numberPhone = userDemand.getPhoneNumber();
        holder.textPhone.setText(numberPhone);

        holder.btnCall.setOnClickListener(v->{
            Intent phoneCallIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + userDemand.getPhoneNumber()));
            context.startActivity(phoneCallIntent);
        });

    }

    @Override
    public int getItemCount() {
        return userDemandList.size();
    }

    public  static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView textName,textPhone;
        private final MaterialButton btnCall;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            textName = itemView.findViewById(R.id.full_name);
            textPhone = itemView.findViewById(R.id.user_phone);
            btnCall = itemView.findViewById(R.id.btn_contact_user_demand);


        }
    }
}
