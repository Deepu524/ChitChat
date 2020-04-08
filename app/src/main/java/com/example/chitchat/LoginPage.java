package com.example.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private Button loginbtn, phonebtn;
    private EditText loginemail, loginpassword;
    private TextView createnewaccount, forgetpassword;
    private ProgressDialog loadingbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        InitializeFields();

        createnewaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AllowUserToLogin();
            }
        });

        phonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent phnLgnIntent = new Intent(LoginPage.this, Phone_Login.class);
                startActivity(phnLgnIntent);
            }
        });
    }

    private void AllowUserToLogin()
    {
        String email = loginemail.getText().toString();
        String password = loginpassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            loginemail.setError("Please Enter an email");
        }

        if (TextUtils.isEmpty(password))
        {
            loginpassword.setError("Please Enter a Password");
        }

        else
        {
            loadingbar.setTitle("Sign In");
            loadingbar.setMessage("Please wait...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();

                                Toast.makeText(LoginPage.this, "Logged in Succeessfully...", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }

                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginPage.this, "Error :"+ message, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        loginbtn = findViewById(R.id.login_lgn_btn);
        phonebtn = findViewById(R.id.login_phn_btn);
        loginemail = findViewById(R.id.login_email);
        loginpassword = findViewById(R.id.login_pass);
        createnewaccount = findViewById(R.id.login_create);
        forgetpassword = findViewById(R.id.login_forgt_pass);

        loadingbar = new ProgressDialog(this);
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginPage.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginPage.this, RegisterPage.class);
        startActivity(registerIntent);
    }
}
