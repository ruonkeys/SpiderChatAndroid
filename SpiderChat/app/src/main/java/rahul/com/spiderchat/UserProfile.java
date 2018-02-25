package rahul.com.spiderchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.text.StringPrepParseException;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rahul on 28/7/17.
 */

public class UserProfile extends AppCompatActivity {
    private ImageView userImg;
    private TextView userDisp, userStatus, userFriendsCount;
    private Button sendReqBtn, declineReqBtn;

    private DatabaseReference mDatabaseUserRef;
    private DatabaseReference mFriendReqDbRef;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationsDb;
    private DatabaseReference mRootRef;

    private ProgressDialog progressDialog;

    private String current_state;

    private String mUser_id;

    //currently logged in user
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        current_state = "not_friends";

        //get intent
        mUser_id = getIntent().getStringExtra("user_id");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        progressDialog = new ProgressDialog(UserProfile.this);
        progressDialog.setTitle("Loading User data");
        progressDialog.setMessage("Please wait while user data is loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mDatabaseUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mUser_id);
        mFriendReqDbRef = FirebaseDatabase.getInstance().getReference().child("FriendReq");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationsDb = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        userImg = (ImageView) findViewById(R.id.user_profile_img);
        userDisp = (TextView) findViewById(R.id.user_profile_displayName);
        userStatus = (TextView) findViewById(R.id.user_profile_status);
        userFriendsCount = (TextView) findViewById(R.id.user_profile_friends_count);
        sendReqBtn = (Button) findViewById(R.id.user_profile_sendRequest_btn);
        declineReqBtn = (Button) findViewById(R.id.user_profile_declineRqst_btn);
        declineReqBtn.setEnabled(false);
        declineReqBtn.setVisibility(View.INVISIBLE);

        if(currentUser.getUid().equals(mUser_id))
        {
            sendReqBtn.setEnabled(false);
            sendReqBtn.setVisibility(View.INVISIBLE);
            declineReqBtn.setEnabled(false);
            declineReqBtn.setVisibility(View.INVISIBLE);
        }

        mDatabaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String img = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                userDisp.setText(name);
                userStatus.setText(status);
                Picasso.with(UserProfile.this).load(img).placeholder(R.drawable.avatar).into(userImg);

                ///////friend list/request feature/////////
                mFriendReqDbRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(mUser_id))
                        {
                            String req_type = dataSnapshot.child(mUser_id).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                                current_state = "req_received";
                                sendReqBtn.setText("Accept Friend Request");
                                declineReqBtn.setEnabled(true);
                                declineReqBtn.setVisibility(View.VISIBLE);
                            }
                            else if(req_type.equals("sent"))
                            {
                                current_state = "req_sent";
                                sendReqBtn.setText("Cancel Friend Request");
                            }
                        }
                        else
                        {
                            mFriendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mUser_id))
                                    {
                                        current_state = "friends";
                                        sendReqBtn.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendReqBtn.setEnabled(false);

                ////////////not_friends state/////////////////
                if(current_state.equals("not_friends"))
                {
//                    mFriendReqDbRef.child(currentUser.getUid()).child(mUser_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful())
//                            {
//                                mFriendReqDbRef.child(mUser_id).child(currentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        HashMap<String, String> notifyData = new HashMap<String, String>();
//                                        notifyData.put("from",currentUser.getUid());
//                                        notifyData.put("type","request");
//
//                                        mNotificationsDb.child(mUser_id).push().setValue(notifyData).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if(task.isSuccessful())
//                                                {
//                                                    sendReqBtn.setEnabled(true);
//                                                    current_state = "req_sent";
//                                                    sendReqBtn.setText("Cancel Friend Request");
//                                                    Toast.makeText(UserProfile.this,"Friend Request sent successfully",Toast.LENGTH_SHORT).show();
//                                                }
//                                                else
//                                                {
//                                                    Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                                    }
//                                });
//                            }
//                            else
//                            {
//                                Toast.makeText(UserProfile.this,"Friend Request not sent",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

                    //code here is quite messy since we're running nested queries also it is bit slow
                    //there is a better way to write complex queries in firebase
                    DatabaseReference newNotification = mNotificationsDb.child(mUser_id).push();
                    String newNotificationId = newNotification.getKey();

                    HashMap<String, String> notifyData = new HashMap<String, String>();
                    notifyData.put("from",currentUser.getUid());
                    notifyData.put("type","request");

                    Map reqMap = new HashMap();
                    reqMap.put("FriendReq/"+currentUser.getUid()+"/"+mUser_id+"/request_type","sent");
                    reqMap.put("FriendReq/"+mUser_id+"/"+currentUser.getUid()+"/request_type","received");
                    reqMap.put("notifications/"+mUser_id+"/"+newNotificationId,notifyData);

                    mRootRef.updateChildren(reqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null)
                            {
                                Toast.makeText(UserProfile.this,"Error while sending request",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                sendReqBtn.setEnabled(true);
                                current_state = "req_sent";
                                sendReqBtn.setText("Cancel Friend Request");
                            }
                        }
                    });
                    //this is a better way of writing complex queries
                }

                ///////////////req_sent state/////////////
                if(current_state.equals("req_sent"))
                {
                    //instead of using removeValue function we can use our shorter way of writing complex queries(explained above), to remove values using that method just make the value of node that is to be removed as null
                    mFriendReqDbRef.child(currentUser.getUid()).child(mUser_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendReqDbRef.child(mUser_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            sendReqBtn.setEnabled(true);
                                            current_state = "not_friends";
                                            sendReqBtn.setText("Send Friend Request");
                                            Toast.makeText(UserProfile.this,"Friend Request cancelled",Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(UserProfile.this,"Error while performing the task",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(UserProfile.this,"Error while performing the task",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                //////////////req_received state//////////
                if(current_state.equals("req_received"))
                {
                    Calendar c = Calendar.getInstance();
                    final int seconds = c.get(Calendar.SECOND);

                    Log.d("TestFilter", "seconds are "+seconds);

                    mFriendDatabase.child(currentUser.getUid()).child(mUser_id).child("date").setValue(seconds).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendDatabase.child(mUser_id).child(currentUser.getUid()).child("date").setValue(seconds).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            mFriendReqDbRef.child(currentUser.getUid()).child(mUser_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        mFriendReqDbRef.child(mUser_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    sendReqBtn.setEnabled(true);
                                                                    current_state = "friends";
                                                                    sendReqBtn.setText("Unfriend");
                                                                    declineReqBtn.setEnabled(false);
                                                                    declineReqBtn.setVisibility(View.INVISIBLE);
                                                                    Toast.makeText(UserProfile.this,"Friend Request Accepted",Toast.LENGTH_SHORT).show();
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(UserProfile.this,"Error while performing the task",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(UserProfile.this,"Error while performing the task",Toast.LENGTH_SHORT).show();
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
                ////////////////friends state////////////////
                if(current_state.equals("friends"))
                {
                    mFriendDatabase.child(currentUser.getUid()).child(mUser_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mFriendDatabase.child(mUser_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            sendReqBtn.setEnabled(true);
                                            current_state = "not_friends";
                                            sendReqBtn.setText("Send Friend Request");
                                            Toast.makeText(UserProfile.this, "Unfriend Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ///////decline button
        declineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineReqBtn.setEnabled(false);
                mFriendReqDbRef.child(currentUser.getUid()).child(mUser_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mFriendReqDbRef.child(mUser_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mNotificationsDb.child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    current_state = "not_friends";
                                                    declineReqBtn.setEnabled(false);
                                                    declineReqBtn.setVisibility(View.INVISIBLE);
                                                    sendReqBtn.setText("Send Friend Request");
                                                    Toast.makeText(UserProfile.this,"Friend Request Declined",Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(UserProfile.this,"Error while performing task",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("users").child(currentUser.getUid()).child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootRef.child("users").child(currentUser.getUid()).child("online").setValue(false);
        mRootRef.child("users").child(currentUser.getUid()).child("lastseen").setValue(ServerValue.TIMESTAMP);
    }
}
