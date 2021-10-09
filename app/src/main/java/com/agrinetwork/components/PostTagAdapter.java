package com.agrinetwork.components;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.DialogTagActivity;
import com.agrinetwork.R;
import com.agrinetwork.UserWallActivity;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostTagItem;
import com.agrinetwork.interfaces.CheckboxChangedListener;
import com.agrinetwork.service.TagService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Setter;

public class PostTagAdapter extends RecyclerView.Adapter<PostTagAdapter.ViewHolder> implements Filterable {
    private List<PostTagItem> postTagItemsList;
    private final List<PostTagItem> postTagItemsListOld;
    private final Context context;
    private final TagService tagService;



    @Setter
    private CheckboxChangedListener checkboxChangedListener;


    public PostTagAdapter(List<PostTagItem> postTagItemList, Context context){
        this.postTagItemsList = postTagItemList;
        this.postTagItemsListOld = postTagItemList;
        this.context = context;
        this.tagService = new TagService(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        return new PostTagAdapter.ViewHolder(resultView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PostTagItem postTagItem = postTagItemsList.get(position);

        String tagName = postTagItem.getName();
        holder.checkBox.setText(tagName);

        holder.checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checkboxChangedListener != null) {
              Toast.makeText(context,compoundButton.getText(),Toast.LENGTH_SHORT).show();

//                for(int index = 0; index < postTagItemsList.size(); index++){
//                    if(compoundButton.getText() == postTagItemsList.get(index).getName()){
//                        Toast.makeText(context,"vi tri thu"+ index ,Toast.LENGTH_SHORT).show();
//                        checkboxChangedListener.onChange(checked, index);
//
//                    }
//                }

              checkboxChangedListener.onChange(checked, position);
            }
        });

    }


    @Override
    public int getItemCount() {
        return postTagItemsList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
        private  final CheckBox checkBox;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_id);


        }
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if(strSearch.isEmpty()){
                    postTagItemsList = postTagItemsListOld;
                }
                else {
                    List<PostTagItem> list = new ArrayList<>();

                    for(PostTagItem tagItem : postTagItemsListOld){

                        if (tagItem.getName().toLowerCase().contains(strSearch.toLowerCase())){

                            list.add(tagItem);
                        }
                    }
                    postTagItemsList = list;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = postTagItemsList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                postTagItemsList = (List<PostTagItem>) filterResults.values;
                notifyDataSetChanged();


            }
        };
    }

}
