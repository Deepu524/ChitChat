package com.example.chitchat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

    private String receiverUserID, sendersUserID, current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestBtn, declineMessageRequestbtn;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        sendersUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestBtn = findViewById(R.id.send_msg_request_btn);
        declineMessageRequestbtn = findViewById(R.id.decline_msg_request_btn);
        current_State = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest()
    {
        chatRequestRef.child(sendersUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                current_State = "request_sent";
                                sendMessageRequestBtn.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                current_State = "request_received";
                                sendMessageRequestBtn.setText("Accept Chat request");

                                declineMessageRequestbtn.setVisibility(View.VISIBLE);
                                declineMessageRequestbtn.setEnabled(true);

                                declineMessageRequestbtn.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactsRef.child(sendersUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                current_State = "friends";
                                                sendMessageRequestBtn.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!sendersUserID.equals(receiverUserID))
        {
            sendMessageRequestBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendMessageRequestBtn.setEnabled(false);

                    if (current_State.equals("new"))
                    {
                        SendChatRequest();
                    }

                    if (current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }

                    if (current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }

                    if (current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact()
    {
        contactsRef.child(sendersUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(sendersUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestBtn.setEnabled(true);
                                                current_State = "new";
                                                sendMessageRequestBtn.setText("Send Message");

                                                declineMessageRequestbtn.setVisibility(View.INVISIBLE);
                                                declineMessageRequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
}

    private void AcceptChatRequest()
    {
        contactsRef.child(sendersUserID).child(receiverUserID)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                           if (task.isSuccessful())
                           {
                               contactsRef.child(receiverUserID).child(sendersUserID)
                                       .child("Contacts").setValue("saved")
                                       .addOnCompleteListener(new OnCompleteListener<Void>()
                                       {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task)
                                           {
                                               if (task.isSuccessful())
                                               {
                                                    chatRequestRef.child(sendersUserID).child(receiverUserID)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(sendersUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendMessageRequestBtn.setEnabled(true);
                                                                                    current_State = "friends";
                                                                                    sendMessageRequestBtn.setText("Remove this contact");

                                                                                    declineMessageRequestbtn.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestbtn.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            });
                                               }
                                           }
                                       });
                           }
                    }
                });
    }

    private void CancelChatRequest()
    {
        chatRequestRef.child(sendersUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(sendersUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestBtn.setEnabled(true);
                                                current_State = "new";
                                                sendMessageRequestBtn.setText("Send Message");

                                                declineMessageRequestbtn.setVisibility(View.INVISIBLE);
                                                declineMessageRequestbtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest()
    {
        chatRequestRef.child(sendersUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(sendersUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            sendMessageRequestBtn.setEnabled(true);
                                            current_State = "request_sent";
                                            sendMessageRequestBtn.setText("Cancel Chat Request");
                                        }
                                    });
                        }
                    }
                });
    }
}
