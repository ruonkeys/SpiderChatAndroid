package rahul.com.spiderchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rahul on 25/7/17.
 */

public class RequestsFrag extends Fragment {
    private DatabaseReference mRootRef;
    private FirebaseUser currentUser;

    private ArrayList<UserData> reqPer;
    private ArrayList<String> reqType;
    private ArrayList<String> perId;

    private ListView reqList;
    private ReqAdapter adapter;

    private int count = 0;

    private int indexToRemove;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.frag_req,container,false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        reqPer = new ArrayList<UserData>();
        reqType = new ArrayList<String>();
        perId = new ArrayList<String>();

        reqList = (ListView) v.findViewById(R.id.req_list);

        if(currentUser != null) {
            ////////////////////////if start/////////////////////////
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child("users").child(currentUser.getUid()).child("online").setValue(true);

            mRootRef.child("FriendReq").child(currentUser.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    reqType.add(dataSnapshot.child("request_type").getValue().toString());
                    perId.add(dataSnapshot.getKey());

                    adapter.notifyDataSetChanged();
                    mRootRef.child("users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            reqPer.add(dataSnapshot.getValue(UserData.class));
                            count++;
                            Log.d("msglog",count+" LIST OBJECTS "+reqPer);
                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot)
                {
                    indexToRemove = perId.indexOf(dataSnapshot.getKey());
                    Log.d("msglog","KEy Value is "+indexToRemove);

                    reqType.remove(indexToRemove);
                    perId.remove(indexToRemove);

                    adapter.notifyDataSetChanged();

                    mRootRef.child("users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            reqPer.remove(indexToRemove);
                            //adapter.remove(adapter.getItem(indexToRemove));
                            //reqList.removeViewAt(indexToRemove);

                            adapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });


            ////////////////////////////////if end/////////////////////////////////////////
        }

        adapter = new ReqAdapter(getActivity(), R.layout.allusers_single_row, reqPer, reqType);
        reqList.setAdapter(adapter);

        reqList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent profileIntent = new Intent(getActivity(),UserProfile.class);
                profileIntent.putExtra("user_id",perId.get(i));
                startActivity(profileIntent);
            }
        });

        return v;
    }
}
class ReqViewHolder
{
    private CircleImageView dispImg;
    private TextView dispName;
    private TextView type;

    ReqViewHolder(View view)
    {
        dispImg = (CircleImageView) view.findViewById(R.id.allusers_img);
        dispName = (TextView) view.findViewById(R.id.allusers_dispName);
        type = (TextView) view.findViewById(R.id.allusers_status);
    }

    public CircleImageView getDispImg() {
        return dispImg;
    }

    public TextView getDispName() {
        return dispName;
    }

    public TextView getType() {
        return type;
    }
}
/////////////adapter
class ReqAdapter extends ArrayAdapter<UserData>
{
    private Context context;
    private ArrayList<UserData> userData;
    private ArrayList<String> reqType;

    public ReqAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<UserData> userData,ArrayList<String> reqType) {
        super(context, resource, userData);
        this.context = context;
        this.userData = userData;
        this.reqType = reqType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ReqViewHolder holder = null;
        if(row == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.allusers_single_row,parent,false);
            holder = new ReqViewHolder(row);
            row.setTag(holder);
        }
        else
        {
            holder = (ReqViewHolder) row.getTag();
        }
        try
        {
            holder.getDispName().setText(userData.get(position).getName());
            holder.getType().setText("Request " + reqType.get(position));
            if (!userData.get(position).getThumb_image().equals("default")) {
                Picasso.with(context).load(userData.get(position).getThumb_image()).placeholder(R.drawable.avatar).into(holder.getDispImg());
            }
        }
        catch(Exception e)
        {
            Log.d("msglog","ERROR OCCURRED "+e);
        }
        return row;
    }
}
