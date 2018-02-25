package rahul.com.spiderchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rahul on 25/7/17.
 */

public class FriendsFrag extends Fragment {
    private ListView friendList;
    private ArrayList<UserData> allFriends;
    private ArrayList<String> frndShpDate;
    private DatabaseReference mFriendsDb;
    private DatabaseReference mUserDb;
    private FirebaseUser currentUser;
    private FriendsDataAdapter adapter;
    private ArrayList<String> allFrndsKeys;

    private boolean online_status;

    private ArrayList<String> frndImg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_friend,container,false);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
        }

        frndShpDate = new ArrayList<String>();
        allFriends = new ArrayList<UserData>();
        allFrndsKeys = new ArrayList<String>();
        frndImg = new ArrayList<String>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            mFriendsDb = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser.getUid());
            mUserDb = FirebaseDatabase.getInstance().getReference().child("users");

            mFriendsDb.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    frndShpDate.add(dataSnapshot.child("date").getValue().toString());

                    mUserDb.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("online")) {
                                online_status = (boolean) dataSnapshot.child("online").getValue();
                            }

                            UserData userData = dataSnapshot.getValue(UserData.class);
                            allFriends.add(userData);
                            frndImg.add(dataSnapshot.child("thumb_image").getValue().toString());
                            //Log.d("testing","all friends objs "+allFriends);

                            allFrndsKeys.add(dataSnapshot.getKey().toString());
                            Log.d("testing", "all ids " + allFrndsKeys);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
        }

        friendList = (ListView) view.findViewById(R.id.friends_list);
        adapter = new FriendsDataAdapter(getActivity(),R.layout.allusers_single_row,allFriends,frndShpDate,online_status);
        friendList.setAdapter(adapter);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int pos, long l) {

                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0)
                        {
                            Intent profileIntent = new Intent(getActivity(),UserProfile.class);
                            profileIntent.putExtra("user_id",allFrndsKeys.get(pos));
                            startActivity(profileIntent);
                        }
                        else if(i == 1)
                        {
                            Intent chatIntent = new Intent(getActivity(),ChatActivity.class);
                            chatIntent.putExtra("user_id",allFrndsKeys.get(pos));
                            chatIntent.putExtra("user_name",allFriends.get(pos).getName());
                            chatIntent.putExtra("frnd_image",frndImg.get(pos));
                            startActivity(chatIntent);
                        }
                    }
                });
                builder.show();

            }
        });

        return view;
    }
}
class FrndsViewHolder
{
    private CircleImageView img;
    private TextView dispName;
    private TextView dateDisp;
    private ImageView onlineImg;

    FrndsViewHolder(View view)
    {
        this.img = (CircleImageView) view.findViewById(R.id.allusers_img);
        this.dispName = (TextView) view.findViewById(R.id.allusers_dispName);
        this.dateDisp = (TextView) view.findViewById(R.id.allusers_status);
        this.onlineImg = (ImageView) view.findViewById(R.id.onlineImage);
    }

    public ImageView getOnlineImg() {return onlineImg;}

    public CircleImageView getImg() {return img;}

    public TextView getDispName() {return dispName;}

    public TextView getDateDisp() {return dateDisp;}
}
class FriendsDataAdapter extends ArrayAdapter<UserData>
{
    private Context context;
    private ArrayList<UserData> frndList;
    private ArrayList<String> dates;
    private boolean online_status;

    public FriendsDataAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserData> objects, List<String> dates, boolean online_status) {
        super(context, resource, objects);
        this.context = context;
        this.frndList = (ArrayList<UserData>) objects;
        this.dates = (ArrayList<String>) dates;
        this.online_status = online_status;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View row = convertView;
        FrndsViewHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.allusers_single_row, parent, false);
            holder = new FrndsViewHolder(row);
            row.setTag(holder);
        }
        else
        {
            holder = (FrndsViewHolder) row.getTag();
        }

        if(!frndList.get(position).getThumb_image().equals("default"))
        {
            Picasso.with(context).load(frndList.get(position).getThumb_image()).placeholder(R.drawable.avatar).into(holder.getImg());
        }
        holder.getDispName().setText(frndList.get(position).getName());
        holder.getDateDisp().setText(dates.get(position));

        if(online_status == true)
        {
            holder.getOnlineImg().setVisibility(View.VISIBLE);
        }
        else if (online_status == false)
        {
            holder.getOnlineImg().setVisibility(View.INVISIBLE);
        }

        return row;
    }
}
