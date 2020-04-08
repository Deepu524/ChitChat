package com.example.chitchat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMsgTxt, receiverMsgTxt;
        public CircleImageView receiverProfileImg;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMsgTxt = (TextView) itemView.findViewById(R.id.sender_msg_txt);
            receiverMsgTxt = (TextView) itemView.findViewById(R.id.receiver_msg_txt);
            receiverProfileImg = (CircleImageView) itemView.findViewById(R.id.msg_profile_img);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImg = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImg).placeholder(R.drawable.profile_image).into(holder.receiverProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        if (fromMessageType.equals("text"))
        {
            holder.receiverMsgTxt.setVisibility(View.INVISIBLE);
            holder.receiverProfileImg.setVisibility(View.INVISIBLE);
            holder.senderMsgTxt.setVisibility(View.INVISIBLE);


            if (fromUserID.equals(messageSenderID))
            {
                holder.senderMsgTxt.setVisibility(View.VISIBLE);

                holder.senderMsgTxt.setBackgroundResource(R.drawable.senders_messages_layout);
                holder.senderMsgTxt.setTextColor(Color.BLACK);
                holder.senderMsgTxt.setText(messages.getMessage());
            }
            else
            {
                holder.receiverMsgTxt.setVisibility(View.VISIBLE);
                holder.receiverProfileImg.setVisibility(View.VISIBLE);

                holder.receiverMsgTxt.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMsgTxt.setTextColor(Color.BLACK);
                holder.receiverMsgTxt.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

}
