package com.agrinetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.components.SearchAdapter;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchUserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private List<User> users = new ArrayList<>();
    private UserService userService;
    private TextView showTextNoResult;
    private MaterialToolbar btnUndo;
    private TextInputEditText userQuery;


    @Override
    protected void onCreate( Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        userService = new UserService(this);
        recyclerView = findViewById(R.id.result_list);

        Intent intent = getIntent();
        String searchUser =  intent.getExtras().getString("search");
        userQuery = findViewById(R.id.search_user);
        userQuery.setText(searchUser);


        btnUndo = findViewById(R.id.toolbar);

        btnUndo.setNavigationOnClickListener(v -> {
            Intent intentUserFeed = new Intent(this,UserFeedActivity.class);
            this.startActivity(intentUserFeed);

        });



        searchAdapter = new SearchAdapter(users, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(searchAdapter);
        showTextNoResult = findViewById(R.id.no_result);


        userQuery.setOnEditorActionListener((textView, i, keyEvent) -> {
            String query = textView.getText().toString();
            if(i == EditorInfo.IME_ACTION_SEARCH){
                Intent intentSearchUser = new Intent(this,SearchUserActivity.class);
                intentSearchUser.putExtra("search",query);
                this.startActivity(intentSearchUser);
                return true;
            }
            return false;
        });

        fetchResultUser(searchUser);

    }
    private  void fetchResultUser(String searchText){
        Call searchUser = userService.searchByUser(searchText);
        searchUser.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call searchByUser, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call searchByUser, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    Gson gson = new Gson();
                    String responseListUser = response.body().string();

                    Type userListType = new TypeToken<List<User>>(){}.getType();
                    List<User> userList = gson.fromJson(responseListUser, userListType);
                 //   System.out.println(userList);

                    SearchUserActivity.this.runOnUiThread(()-> {
                        if(!userList.isEmpty()) {
                            users.addAll(userList);
                            searchAdapter.notifyDataSetChanged();
                        }
                        else {
                            showTextNoResult.setVisibility(View.VISIBLE);
                        }

                    });

                }

            }
        });

    }
}
