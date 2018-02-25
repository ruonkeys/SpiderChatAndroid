package rahul.com.spiderchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by rahul on 25/7/17.
 */

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText newStatus;
    private Button saveBtn;

    private ProgressDialog mProgress;

    //firebase
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_activity);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newStatus = (EditText) findViewById(R.id.status_edit);
        saveBtn = (Button) findViewById(R.id.status_save_btn);

        String status_value = getIntent().getStringExtra("status_value");
        newStatus.setText(status_value);

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while saving changes...");
                mProgress.show();

                mDatabase.child("status").setValue(newStatus.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this,"Error while saving changes",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
