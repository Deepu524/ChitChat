package com.example.chitchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View requestsFragmentView;

    private RecyclerView myRecyclerList;

    private DatabaseReference chatRequestsRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView =  inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRecyclerList = (RecyclerView) requestsFragmentView.findViewById(R.id.chat_requests_list);
        myRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestsFragmentView;
    }
    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter
                = new FirebaseRecyclerAdapter <Contacts, RequestViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("received"))
                            {
                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild("image"))
                                        {
                                            final String requestsProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestsProfileImage).into(holder.profileImage);
                                        }
                                            final String requestsUserName = dataSnapshot.child("name").getValue().toString();
                                            final String requestsUserStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(requestsUserName);
                                            holder.userStatus.setText("Wants to connect with you.");

                                            holder.itemView.setOnClickListener(new View.OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestsUserName + "Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i)
                                                    {
                                                        if (i == 0)
                                                        {
                                                            contactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        contactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                                    {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "New Contacts Saved", Toast.LENGTH_SHORT).show();
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (i == 1)
                                                        {
                                                            chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "Contacts Deleted", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            else if (type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");

                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild("image"))
                                        {
                                            final String requestsProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestsProfileImage).into(holder.profileImage);
                                        }
                                        final String requestsUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestsUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestsUserName);
                                        holder.userStatus.setText("You have sent a request to " + requestsUserName);

                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i)
                                                    {
                                                        if (i == 0)
                                                        {
                                                            chatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                chatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "You have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return  viewHolder;
            }
        };

        myRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptBtn, cancelBtn;

        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
