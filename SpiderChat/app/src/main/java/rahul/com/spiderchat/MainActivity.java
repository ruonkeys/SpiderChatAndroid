package rahul.com.spiderchat;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDb;

    private FirebaseUser currentUser;

    private Toolbar toolbar;

    private ViewPager viewPager;
    private PagerSlidingTabStrip tabStrip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            mUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }

        toolbar = (Toolbar) findViewById(R.id.main_pg_bar);
        //to use setSupportActionBar method, must extend AppCompatActivity instead of Activity
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PharmaChat");

        viewPager = (ViewPager) findViewById(R.id.pager);
        TabSectionAdapter tb = new TabSectionAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tb);

        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            sendToStartActivity();
        }
        if(currentUser != null) {
            mUserDb.child("online").setValue(true);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("rahul","MainActivity onPause called");
        if(currentUser != null) {
            mUserDb.child("online").setValue(false);
            mUserDb.child("lastseen").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStartActivity()
    {
        Intent intent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();//preventing to come back to this activity using back button
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.logout_btn)
        {
            mUserDb.child("online").setValue(false);
            mUserDb.child("lastseen").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStartActivity();
        }

        if(item.getItemId() == R.id.account_btn)
        {
            Intent account_intent = new Intent(MainActivity.this,AccountSettings.class);
            startActivity(account_intent);
        }

        if(item.getItemId() == R.id.all_btn)
        {
            Intent users_intent = new Intent(MainActivity.this,AllUsers.class);
            startActivity(users_intent);
        }
        return true;
    }
}
