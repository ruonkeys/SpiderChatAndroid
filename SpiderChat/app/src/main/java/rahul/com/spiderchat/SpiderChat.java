package rahul.com.spiderchat;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by rahul on 28/7/17.
 */

public class SpiderChat extends Application {
    private DatabaseReference mUserDb;

    @Override
    public void onCreate() {
        super.onCreate();

        //for enabling offline capabilities of firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //for enabling offline capabilities of Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            //for online offline feature
            mUserDb = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            mUserDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserDb.child("online").onDisconnect().setValue(false);
                        mUserDb.child("lastseen").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
