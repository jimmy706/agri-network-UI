package com.agrinetwork;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.agrinetwork.config.Variables;
import com.agrinetwork.helpers.TextValidator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;


import java.util.Objects;


public class MainActivity extends Activity {

    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth firebaseAuth;

    TextInputEditText inputEmail, passwordInput;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton loginButton = findViewById(R.id.login_btn);
        inputEmail = findViewById(R.id.edit_text_email);
        passwordInput = findViewById(R.id.edit_text_password);
        TextView moveToRegisterLink = findViewById(R.id.move_to_register);

        validateFields();

        firebaseAuth = FirebaseAuth.getInstance();

        // Check network connection
        if(!isNetworkConnected()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.no_network));
            alertDialogBuilder.setMessage(getResources().getString(R.string.check_network));

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        moveToRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(view -> {
            String email = Objects.requireNonNull(inputEmail.getText()).toString();
            String password = Objects.requireNonNull(passwordInput.getText()).toString();

           firebaseAuth.signInWithEmailAndPassword(email, password)
                   .addOnCompleteListener(this, task -> {
                        if(task.isSuccessful()) {
                            String idToken = Objects.requireNonNull(task.getResult().getUser()).getIdToken(true).getResult().getToken();
                            MainActivity.this.runOnUiThread(()-> {
                                Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                setSharedToken(idToken);
                                startActivity(new Intent(MainActivity.this, UserFeedActivity.class));
                            });
                        }
                        else{
                            Log.w("LoginFailed", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                   });
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account != null) {
            redirectToFeedActivity(account);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            currentUser.getIdToken(false)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            MainActivity.this.runOnUiThread(()-> {
                                setSharedToken(idToken);
                                startActivity(new Intent(MainActivity.this, UserFeedActivity.class));
                            });
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGgSignInResult(task);
        }
    }

    private void handleGgSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

        }
        catch (ApiException e) {
            Log.w(Integer.toString(e.getStatusCode()), "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void redirectToFeedActivity(GoogleSignInAccount account) {
        Log.i("GGAcount", account.getIdToken());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return Objects.nonNull(cm.getActiveNetworkInfo()) && cm.getActiveNetworkInfo().isConnected();

    }

    private void validateFields() {
        inputEmail.addTextChangedListener(new TextValidator(inputEmail) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.email_required));
                }
            }
        });
        passwordInput.addTextChangedListener(new TextValidator(passwordInput) {
            @Override
            public void validate(TextView textView, String value) {
                if(value.isEmpty()) {
                    textView.setError(getText(R.string.password_required));
                }
            }
        });
    }

    private void setSharedToken(String idToken) {
        // Store id app's data
        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Variables.ID_TOKEN_LABEL, idToken);
        editor.apply();
    }
}