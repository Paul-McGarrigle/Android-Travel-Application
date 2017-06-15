package com.example.paulm.travelapp;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import models.User;

public class UserHomeActivity extends Activity {
    // The number is to identify which activity has been returned from
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PHOTO_TAKEN = 0;
    private ImageView imageView;
    private View textView;
    private EditText editText;
    private File image;
    private User passedUser;
    private Uri fileUri;
    private ArrayList<User> userList = new ArrayList<User>();
    //public static final int MEDIA_TYPE_IMAGE = 1;
    // Firebase Realtime Database references
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("user");

    private String result;

    // essential URL structure is built using constants
    public static final String ACCESS_KEY = "347b7abc9391d65585b6aa71c3e61577";
    public static final String BASE_URL = "http://apilayer.net/api/";
    public static final String ENDPOINT = "live";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // User Object passed from LoginActivity, may need to change final
        passedUser = (User)getIntent().getSerializableExtra("user");
        textView = (TextView)this.findViewById(R.id.userInfo);
        ((TextView)textView).setText("User Name: " + passedUser.getName() + "\nCountry: " + passedUser.getCountry()
                + "\nTap Photo to change Profile Picture");

        //editText = (EditText)findViewById(R.id.edit);

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


        ///////////////////////////////////////////////////////////////////////////////
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected()){
            new MyAsyncTask().execute();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Result in main thread: " + result);
            ((EditText)findViewById(R.id.edit)).setText(result);
        } else {
            Toast.makeText(this, "No Connection!", Toast.LENGTH_LONG).show();
            ((EditText)findViewById(R.id.edit)).setText("No Internet Connection");
        }

        Button exchange = (Button)findViewById(R.id.exchangeBtn);
        exchange.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {

                        try {
                            return downloadHTML();
                        } catch (Exception e) {
                            Log.d("JWP", e.toString());
                        }

                        return "Can't reach server. Is Internet access enabled?";
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        TextView textView = (TextView)findViewById(R.id.edit);

                        try {
                            JSONArray items = (JSONArray)(new JSONTokener(result).nextValue());
                            String c = "";
                            for(int i=0; i<items.length(); i++) {
                                JSONObject item = (JSONObject)items.get(i);
                                String name = item.getString("name");

								/*JSONArray currencies = (JSONArray)(new JSONTokener(name).nextValue());
								for(int j=0; j<currencies.length(); j++) {
									JSONObject currency = (JSONObject)currencies.get(j);
									c = item.getString("currencies");
								}*/
                                //String currency = item.get("currencies").getString("name");
								/*JSONArray currencies = (JSONArray)(new JSONTokener("currencies").nextValue());
								for(int j=0; j<currencies.length(); j++) {
									JSONObject currency = (JSONObject)currencies.get(j);
									c = currency.getString("name");
								}*/
                                //String flag = item.getString("flag");

                                //textView.append(name);
                                //textView.append("\n");
                                ((EditText)findViewById(R.id.edit)).setText(name+"\n");
                                //editText.append(name);
                                //editText.append("\n");

                                //textView.append(c);
                                //textView.append("\n");
                                //ImageView img = (ImageView)findViewById(R.id.img);
                                //img.setImageBitmap((Bitmap)flag);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                }.execute();

            }

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
                            mRootRef.child(userName).getRef().child("img").setValue(photo.toString());
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

    //////////////////////////////////////////////////////////////////////////////////////////////////
    class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            StringBuffer sb = new StringBuffer();
            URL url = null;
            try {
                url = new URL(BASE_URL+ENDPOINT+ACCESS_KEY);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection c = null;

            try {
                c = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                c.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

            InputStream in = null;
            try {
                System.out.println("Responce code: " + c.getResponseCode());
                in = new BufferedInputStream(c.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = "";

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            setResult(sb.toString());
            System.out.println("Responce Json: " + sb.toString());

            return sb.toString();
        }
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void selectFrag(View view) {
        // MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);
        //mp.start();
        Fragment fr = null;

        /*if(view == findViewById(R.id.button2)) {
            fr = new FragmentTwo();

        }else {
            fr = new FragmentOne();
        }*/

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, fr);
        fragmentTransaction.commit();
    }

    private String downloadHTML() throws Exception {

        String id = "99";
        String password = URLEncoder.encode("lj=lj&kljl/kj/", "UTF-8");

		/*
		 * URL url = new URL(
		 * "http://androidserver.picturesquirrel.cloudbees.net/Server?id=" + id
		 * + "&pass=" + password);
		 */

        URL url = new URL("https://restcountries.eu/rest/v2/name/ireland");

        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        // Use a StringBuilder to collect the lines of
        // data we retrieve.
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }
}
