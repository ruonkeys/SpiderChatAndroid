package rahul.com.spiderchat;

import android.app.Activity;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

/**
 * Created by rahul on 18/7/17.
 */

public class RegisterActivity extends AppCompatActivity {
    private EditText disp;
    private EditText eml;
    private EditText pass;
    private Button createBtn;

    //progress bar
    private ProgressDialog progressDialog;

    //Toolbar
    private Toolbar toolbar;

    //FirebaseAuth
    private FirebaseAuth mAuth;

    //Firebase database reference
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_act_des);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //Progress bar
        progressDialog = new ProgressDialog(this);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.reg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        //setting back button in toolbar, to have it working you must set parentActivity in ManifestFile for this activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Android fields
        disp = (EditText) findViewById(R.id.reg_disp);
        eml = (EditText) findViewById(R.id.reg_email);
        pass = (EditText) findViewById(R.id.reg_pass);
        createBtn = (Button) findViewById(R.id.create_btn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = disp.getText().toString();
                String email = eml.getText().toString();
                String password = pass.getText().toString();

                if(displayName.isEmpty() || email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this,"fill all the fields",Toast.LENGTH_SHORT);
                }
                else
                {
                    progressDialog.setTitle("Registering account");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    registerUser(displayName,email,password);
                }
            }
        });
    }

    private void registerUser(final String displayName, String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                    userMap.put("name",displayName);
                    userMap.put("status","Hi! there i'm using SpiderChat");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);

                                //by adding these flags we are actually starting a new task i.e on pressing back button we'll go back to our device's screen only not to any other activity
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    String error;
                    progressDialog.hide();
                    try
                    {
                        throw task.getException();
                    }
                    catch(FirebaseAuthWeakPasswordException e)
                    {
                        error = "Weak password";
                    }
                    catch(FirebaseAuthUserCollisionException e)
                    {
                        error = "Account already exists";
                    }
                    catch(FirebaseAuthInvalidCredentialsException e)
                    {
                        error = "Invalid email";
                    }
                    catch(Exception e)
                    {
                        error = "Unknown error";
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this,error,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
