package rahul.com.spiderchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rahul on 12/8/17.
 */

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference mRootRef;
    private String frndName;
    private String frnd_id;
    private FirebaseUser currentUser;

    private ImageButton addBtn;
    private ImageButton sendBtn;
    private EditText typeMsg;

    private String frndImg;

    private ListView chatList;
    private ArrayList<Messages> messageList;
    private ChatAdapter chatAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mToolbar = (Toolbar) findViewById(R.id.chat_activity_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        Intent intent = getIntent();
        frndName = intent.getStringExtra("user_name");
        frnd_id = intent.getStringExtra("user_id");
        frndImg = intent.getStringExtra("frnd_image");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        //toolbar stuff
        TextView frndNameVw = (TextView) findViewById(R.id.chat_activity_DispName);
        final TextView lastSeenVw = (TextView) findViewById(R.id.chat_activity_lastSeen);
        CircleImageView frndImgVw = (CircleImageView) findViewById(R.id.chat_activity_img);

        frndNameVw.setText(frndName);

        if(!frndImg.equals("default"))
        {
            Picasso.with(ChatActivity.this).load(frndImg).placeholder(R.drawable.avatar).into(frndImgVw);
        }

        //send stuff
        addBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        sendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        typeMsg = (EditText) findViewById(R.id.chat_type_msg);

        //chat list
        chatList = (ListView) findViewById(R.id.chat_act_msgList);

        ////////////////online last seen, image start///////////////////////////
        DatabaseReference userDb = mRootRef.child("users").child(frnd_id);
        userDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                frndImg = (String) dataSnapshot.child("thumb_image").getValue();
                if(dataSnapshot.hasChild("online"))
                {
                    boolean online_status = (boolean) dataSnapshot.child("online").getValue();
                    if(online_status)
                    {
                        lastSeenVw.setText("online");
                    }
                    else
                    {
                        if(dataSnapshot.hasChild("lastseen"))
                        {
                            String lastSeen = dataSnapshot.child("lastseen").getValue().toString();

                            TimeAgo timeAgo = new TimeAgo();
                            Long lastSeenTime = Long.parseLong(lastSeen);
                            lastSeen = timeAgo.getTimeAgo(lastSeenTime,ChatActivity.this);

                            lastSeenVw.setText("last seen "+lastSeen);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ////////////////////online last seen end///////////////

        ////////////////chat ///////////////
        mRootRef.child("chat").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(frnd_id))
                {
                    Map chatAddDataMap = new HashMap();
                    chatAddDataMap.put("seen",false);
                    chatAddDataMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/"+currentUser.getUid()+"/"+frnd_id,chatAddDataMap);
                    chatUserMap.put("chat/"+frnd_id+"/"+currentUser.getUid(),chatAddDataMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null)
                            {
                                Toast.makeText(ChatActivity.this,"Error "+databaseError.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        ////////////////chat list/////////////////
        messageList = new ArrayList<Messages>();

        mRootRef.child("messages").child(currentUser.getUid()).child(frnd_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages msg = dataSnapshot.getValue(Messages.class);
                messageList.add(msg);
                chatAdapter.notifyDataSetChanged();
                chatList.smoothScrollToPosition(messageList.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatAdapter = new ChatAdapter(ChatActivity.this, R.layout.msg_row, messageList, frndImg);
        chatList.setAdapter(chatAdapter);
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

    /////saving messages to database
    private void sendMessage()
    {
        String message = typeMsg.getText().toString();
        if(!message.isEmpty())
        {
            String current_user_ref = "messages/"+currentUser.getUid()+"/"+frnd_id;
            String friend_ref = "messages/"+frnd_id+"/"+currentUser.getUid();

            DatabaseReference msg_push = mRootRef.child("messages").child(currentUser.getUid()).child(frnd_id).push();
            String push_id = msg_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("type","text");
            messageMap.put("seen",false);
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUser.getUid());

            Map msgUserMap = new HashMap();
            msgUserMap.put(current_user_ref+"/"+push_id,messageMap);
            msgUserMap.put(friend_ref+"/"+push_id,messageMap);

            mRootRef.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null)
                    {
                        Toast.makeText(ChatActivity.this,"Error "+databaseError.toString(),Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        typeMsg.setText("");
                    }
                }
            });
        }
    }
}
//////////view holder//////////
class ChatViewHolder
{
    private CircleImageView imgVw;
    private TextView msgVw;
    private TextView msgSendVw;

    ChatViewHolder(View view)
    {
        imgVw = (CircleImageView) view.findViewById(R.id.msg_sender_img);
        msgVw = (TextView) view.findViewById(R.id.msg_cntnt);
        msgSendVw = (TextView) view.findViewById(R.id.msg_cntnt_sent);
    }

    public CircleImageView getImgVw() {
        return imgVw;
    }

    public TextView getMsgVw() {
        return msgVw;
    }

    public TextView getMsgSendVw() {
        return msgSendVw;
    }
}

////////adapter/////////////
class ChatAdapter extends ArrayAdapter<Messages>
{
    private Context context;
    private ArrayList<Messages> msgs;
    private String frndImg;
    public ChatAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Messages> msgs, String frndImg) {
        super(context, resource, msgs);
        this.context = context;
        this.msgs = msgs;
        this.frndImg = frndImg;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ChatViewHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.msg_row, parent, false);
            holder = new ChatViewHolder(row);
            row.setTag(holder);
        }
        else
        {
            holder = (ChatViewHolder) row.getTag();
        }
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(msgs.get(position).getFrom()))
        {
            holder.getMsgSendVw().setVisibility(View.VISIBLE);
            holder.getMsgSendVw().setText(msgs.get(position).getMessage());
            holder.getMsgVw().setVisibility(View.GONE);
            holder.getImgVw().setVisibility(View.GONE);
        }
        else
        {
            holder.getMsgVw().setVisibility(View.VISIBLE);
            holder.getImgVw().setVisibility(View.VISIBLE);
            holder.getMsgVw().setText(msgs.get(position).getMessage());
            holder.getMsgSendVw().setVisibility(View.GONE);
        }
        if(!frndImg.equals("default"))
        {
            Picasso.with(context).load(frndImg).placeholder(R.drawable.avatar).into(holder.getImgVw());
        }
        return row;
    }
}
