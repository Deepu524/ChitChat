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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterPage extends AppCompatActivity {

    private Button createaccountbtn;
    private EditText registeremail, registerpassword;
    private TextView alreadyhaveanaccount;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        alreadyhaveanaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        createaccountbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createnewaccount();
            }
        });
    }

    private void createnewaccount() {

        String email = registeremail.getText().toString();
        String password = registerpassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            registeremail.setError("Please Enter an email");
        }

        if (TextUtils.isEmpty(password))
        {
            registerpassword.setError("Please Enter a Password");
        }

        else
        {
            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please wait, While we are creating new account for you...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserID).setValue("");

                                SendUserToMainActivity();
                                Toast.makeText(RegisterPage.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }

                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterPage.this, "Error :"+ message, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }

                    });
        }

    }

    private void InitializeFields() {

        createaccountbtn = findViewById(R.id.register_create_btn);
        registeremail = findViewById(R.id.register_email);
        registerpassword = findViewById(R.id.register_pass);
        alreadyhaveanaccount = findViewById(R.id.register_alreadyaccount);

        loadingbar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterPage.this, LoginPage.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterPage.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
