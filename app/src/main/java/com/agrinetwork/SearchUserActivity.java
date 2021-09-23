package com.agrinetwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.components.SearchAdapter;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchUserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private List<User> users = new ArrayList<>();
    private UserService userService;


    @Override
    protected void onCreate( Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        userService = new UserService(this);
        recyclerView = findViewById(R.id.result_list);

        Intent intent = getIntent();
        String searchUser =  intent.getExtras().getString("search");
        EditText resultText = findViewById(R.id.result_text);
        resultText.setText(searchUser);

        searchAdapter = new SearchAdapter(users, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);


        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(searchAdapter);

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

                    SearchUserActivity.this.runOnUiThread(()-> {
                        users.addAll(userList);
                        searchAdapter.notifyDataSetChanged();

                    });
                }

            }
        });

    }
}
