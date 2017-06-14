package com.example.paulm.travelapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import models.User;

public class UserHomeActivity extends Activity {
    // The number is to identify which activity has been returned from
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PHOTO_TAKEN = 0;
    private ImageView imageView;
    private View textView;
    private File image;
    private User passedUser;
    private Uri fileUri;
    private ArrayList<User> userList = new ArrayList<User>();
    //public static final int MEDIA_TYPE_IMAGE = 1;
    // Firebase Realtime Database references
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // User Object passed from LoginActivity, may need to change final
        passedUser = (User)getIntent().getSerializableExtra("user");
        textView = (TextView)this.findViewById(R.id.userInfo);
        ((TextView)textView).setText("User Name: " + passedUser.getName() + "\nCountry: " + passedUser.getCountry()
                + "\nTap Photo to change Profile Picture");

        //isExternalStorageReadable();
        //isExternalStorageWritable();
        // REQUIRED for accessing external storage, taken from stackoverflow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            //your code
        }

        // Use Image View in Layout
        imageView = (ImageView)findViewById(R.id.imageView1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File gallery = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                image = new File(gallery, passedUser.getName()+"profilePic.jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //fileUri = getOutputMediaFileUri(1);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                setResult(Activity.RESULT_OK, intent);
                startActivityForResult(intent, PHOTO_TAKEN);


            }
        });

        if(passedUser.getImg() != ""){
            //byte[] decodedString = Base64.decode(passedUser.getImg(), Base64.DEFAULT);
            //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            //Bitmap photo = BitmapFactory.decodeFile(Uri.fromFile(passedUser.getImg()));
            imageView.setImageURI(Uri.parse(passedUser.getImg()));
        }

        // Add Firebase JSON data to ArrayList for later use
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    userList.add(user);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "Writable", Toast.LENGTH_LONG).show();
            return true;
        }
        Toast.makeText(this, "Not Writable", Toast.LENGTH_LONG).show();
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(this, "Readable", Toast.LENGTH_LONG).show();
            return true;
        }
        Toast.makeText(this, "Not Readable", Toast.LENGTH_LONG).show();
        return false;
    }

    // This must be overwritten to get result of intent, Sub-Activity
    //////////////////Set usb to no
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
        if (requestCode == PHOTO_TAKEN) {
            Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath());

            if (photo != null && resultCode == RESULT_OK) {
                /////////////////See if this can be put back to bitmap from string and should allow image save
                    for (User u : userList) {
                        if (passedUser.getEmail().equals(u.getEmail())) {
                            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            //photo.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                            //byte[] b = baos.toByteArray();
                            u.setImg(photo.toString());
                            String userName = u.getEmail().substring(0, u.getEmail().indexOf("."));
                            //String img = BitMapToString(photo);
                            mRootRef.child(userName).getRef().child("img").setValue(u.getImg());
                        }
                    }
                imageView.setImageBitmap(photo);
                Toast.makeText(this, "WWWWWWWWWW" + photo, Toast.LENGTH_LONG).show();
            } else {
                //imageView.setImageBitmap(photo);
                Toast.makeText(this, "Cannot Save Photo", Toast.LENGTH_LONG).show();

            }
        }
        } catch(NullPointerException e){
            Toast.makeText(this, "Issue Saving Photo, Try Again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            recreate();
        }
    }
}
