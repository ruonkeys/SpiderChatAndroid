package rahul.com.spiderchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rahul on 26/7/17.
 */

public class AllUsers extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Toolbar mToolbar;
    private ListView allUsersListView;

    private DatabaseReference mDatabase;

    private ArrayList<UserData> userDataArrayList;

    private MyAdapter myAdapter;

    //for storing keys
    private ArrayList<String> allKeys;
    private LinkedHashSet<String> allKeysSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_users);

        mToolbar = (Toolbar) findViewById(R.id.allusers_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        userDataArrayList = new ArrayList<UserData>();

        allKeys = new ArrayList<String>();
        allKeysSet = new LinkedHashSet<String>();

        Log.d("MyFilter","DATABASE KEY VALUE IS: "+mDatabase.getKey());

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("MyFilter","reached");
                Log.d("MyFilter","VALUE USING VALUEEVENTLISTENER IS: "+dataSnapshot.getValue()+" String val is: "+s);
                UserData userData = dataSnapshot.getValue(UserData.class);
                Log.d("MyFilter","userData obj value is: "+userData);
                userDataArrayList.add(userData);
                Log.d("MyFilter","ArrayList inside listener: "+userDataArrayList);
                myAdapter.notifyDataSetChanged();

                allKeys.add(dataSnapshot.getKey());
                allKeysSet.addAll(allKeys);
                allKeys.clear();
                allKeys.addAll(allKeysSet);
                allKeysSet.clear();

                Log.d("TestFilter","all keys array is: "+allKeys);


                Log.d("MyFilter","reQUIred key is "+dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        allUsersListView = (ListView) findViewById(R.id.allusers_listView);

        //Log.d("MyFilter","use r DATA arr List "+userDataArrayList);

        myAdapter = new MyAdapter(AllUsers.this,R.layout.allusers_single_row,userDataArrayList);

        allUsersListView.setAdapter(myAdapter);
        //Log.d("MyFilter","after setting adapter");

        allUsersListView.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(false);
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lastseen").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent profileIntent = new Intent(AllUsers.this,UserProfile.class);
        profileIntent.putExtra("user_id",allKeys.get(i));
        startActivity(profileIntent);
    }
}

class MyViewHolder
{
    private CircleImageView imgVw;
    private TextView disp;
    private TextView stat;

    MyViewHolder(View v)
    {
        this.imgVw = (CircleImageView) v.findViewById(R.id.allusers_img);
        this.disp = (TextView) v.findViewById(R.id.allusers_dispName);
        this.stat = (TextView) v.findViewById(R.id.allusers_status);
    }

    public CircleImageView getImgVw() {
        return imgVw;
    }

    public TextView getDisp() {
        return disp;
    }

    public TextView getStat() {
        return stat;
    }
}

class MyAdapter extends ArrayAdapter<UserData>
{
    private ArrayList<UserData> userList;
    private Context context;

    public MyAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserData> objects) {
        super(context, resource, objects);
        this.context = context;
        this.userList = (ArrayList<UserData>) objects;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        MyViewHolder holder = null;
        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.allusers_single_row, parent, false);
            holder = new MyViewHolder(row);
            row.setTag(holder);
        }
        else
        {
            holder = (MyViewHolder) row.getTag();
        }
        if(userList.get(position).getThumb_image().toString() != "default") {
            Picasso.with(context).load(userList.get(position).getThumb_image()).placeholder(R.drawable.avatar).into(holder.getImgVw());
        }
        holder.getDisp().setText(userList.get(position).getName());
        holder.getStat().setText(userList.get(position).getStatus());

        return row;
    }
}
