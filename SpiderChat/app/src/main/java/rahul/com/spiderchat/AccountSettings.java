package rahul.com.spiderchat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

/**
 * Created by rahul on 25/7/17.
 */

public class AccountSettings extends AppCompatActivity {
    private TextView displayName;
    private TextView status;
    private CircleImageView profilePic;
    private Button updateBtn;
    private Button changeImgBtn;

    private static final int GALLERY_PICK = 0;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private String mRandom;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings);

        displayName = (TextView) findViewById(R.id.acnt_dispName);
        status = (TextView) findViewById(R.id.acnt_status);
        profilePic = (CircleImageView) findViewById(R.id.acnt_img);
        updateBtn = (Button) findViewById(R.id.acnt_updateStatus);
        changeImgBtn = (Button) findViewById(R.id.acnt_changePic);

        mStorage = FirebaseStorage.getInstance().getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        mRandom = current_uid.substring(1,8);
        Log.d("MyFilter",mRandom);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        //sinks data offline
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String mstatus = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                displayName.setText(name);
                status.setText(mstatus);

                if(image != "default") {
                    //Picasso.with(AccountSettings.this).load(image).placeholder(R.drawable.avatar).into(profilePic);

                    //this is for offline feature
                    Picasso.with(AccountSettings.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(profilePic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettings.this).load(image).placeholder(R.drawable.avatar).into(profilePic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent status_intent = new Intent(AccountSettings.this,StatusActivity.class);
                status_intent.putExtra("status_value",status.getText().toString());
                startActivity(status_intent);
            }
        });

        changeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("online").setValue(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase.child("online").setValue(false);
        mDatabase.child("lastseen").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap thumb_bitmap = null;

        if(requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgressDialog = new ProgressDialog(AccountSettings.this);
                mProgressDialog.setTitle("Upload Image");
                mProgressDialog.setMessage("Please wait while image is uploading...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mStorage.child("profile_pics").child("profile"+mRandom+".jpg");

                final StorageReference thumb_path = mStorage.child("profile_pics").child("thumb_pics").child("thumb"+mRandom+".jpg");

                //it is good to generate mRandom using Uid, because every time we upload an image old image will be replaced, this saves a lot of storage, also we just nee to store the current profile pic not all the pics
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            @SuppressWarnings("VisibleForTests") final//this is necesssary otherwise error will be shown
                            String img_uri = task.getResult().getDownloadUrl().toString();
//                            Log.d("MyFilter","image uri using task obj: "+test);
//                            String img_uri = filePath.getDownloadUrl().toString();
//                            Log.d("MyFilter","image uri is "+img_uri);

                            UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    @SuppressWarnings("VisibleForTests")//this is necesssary otherwise error will be shown
                                    String thumb_uri = task.getResult().getDownloadUrl().toString();

                                    Map updateMap = new HashMap();
                                    updateMap.put("image",img_uri);
                                    updateMap.put("thumb_image",thumb_uri);

                                    mDatabase.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful())
                                            {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AccountSettings.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(AccountSettings.this, "Error while uploading thumb nail", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(AccountSettings.this,"Error while uploading",Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

//    public static int randomNum(int min,int max)
//    {
//        Random rn = new Random();
//        int range = max - min + 1;
//        int rand =  rn.nextInt(range) + min;
//        return rand;
//    }
}
