package com.example.chitchat;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String msgRecID, msgRecName, msgRecImage, msgSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ImageButton sendMsgBtn;
    private EditText msgInputTxt;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        msgSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        msgRecID = getIntent().getExtras().get("visit_user_id").toString();
        msgRecName = getIntent().getExtras().get("visit_user_name").toString();
        msgRecImage = getIntent().getExtras().get("visit_user_image").toString();


        InitializeController();

        userName.setText(msgRecName);
        Picasso.get().load(msgRecImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMsgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });

        }

    private void InitializeController()
    {
        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userName = (TextView) findViewById(R.id.custom_profile_name);

        sendMsgBtn = (ImageButton) findViewById(R.id.send_private_msg_btn);
        msgInputTxt = (EditText) findViewById(R.id.input_private_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_msgs_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Messages").child(msgSenderID).child(msgRecID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }

    private void sendMessage()
    {
        String msgTxt = msgInputTxt.getText().toString();

        if(TextUtils.isEmpty(msgTxt))
        {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String msgSenderRef = "Messages/" + msgSenderID + "/" + msgRecID;
            String msgRecRef = "Messages/" + msgRecID + "/" + msgSenderID;

            DatabaseReference userMsgKeyRef = rootRef.child("Messages")
                    .child(msgSenderID).child(msgRecID).push();

            String msgPushID = userMsgKeyRef.getKey();

            Map msgTxtBody = new HashMap();
            msgTxtBody.put("message", msgTxt);
            msgTxtBody.put("type", "text");
            msgTxtBody.put("from", msgSenderID);

            Map msgBodyDetails = new HashMap();
            msgBodyDetails.put(msgSenderRef + "/" + msgPushID, msgTxtBody);
            msgBodyDetails.put(msgRecRef + "/" + msgPushID, msgTxtBody);

            rootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    msgInputTxt.setText("");
                }
            });

        }

    }
}
