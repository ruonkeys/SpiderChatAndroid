package rahul.com.spiderchat;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by rahul on 18/7/17.
 */

public class MyLogInActivity extends AppCompatActivity {

    private EditText email;
    private EditText pass;
    private Button logInBtn;

    //toolbar
    private Toolbar mtoolbar;

    //progress bar
    private ProgressDialog progressDialog;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDb;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_des);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        mUserDb = FirebaseDatabase.getInstance().getReference().child("users");

        //progress bar
        progressDialog = new ProgressDialog(this);

        //toolbar
        mtoolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = (EditText) findViewById(R.id.inp_email);
        pass = (EditText) findViewById(R.id.inp_pass);
        logInBtn = (Button) findViewById(R.id.inp_login);

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailTxt = email.getText().toString();
                String passTxt = pass.getText().toString();

                if(emailTxt.isEmpty() || passTxt.isEmpty())
                {
                    Toast.makeText(MyLogInActivity.this,"Please fill all the fields",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Logging in");
                    progressDialog.setMessage("Please wait while logging in");
                    progressDialog.show();

                    logInFn(emailTxt,passTxt);
                }
            }
        });
    }

    public void logInFn(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDb.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                Intent intent = new Intent(MyLogInActivity.this,MainActivity.class);
                                //by adding these flags we are actually starting a new task i.e on pressing back button we'll go back to our device's screen only not to any other activity
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();//preventing to back to this activity after log in success
                            }
                            else
                            {
                                Toast.makeText(MyLogInActivity.this,"Error while storing token",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    progressDialog.hide();
                    Toast.makeText(MyLogInActivity.this, "Error while logging in", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
