package com.example.paulm.travelapp;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import models.Country;
import models.User;

public class UserHomeActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int PHOTO_TAKEN = 0;
    private ImageView imageView;
    private View textView;
    private EditText editText;
    private File image;
    private User passedUser;
    private Uri fileUri;
    private ArrayList<User> userList = new ArrayList<User>();
    private String result;
    private GoogleApiClient googleApiClient;
    private SensorManager mSensorManager;
    private ShakeListener mSensorListener;
    private boolean error = false;
    private Country userCountry, destCountry;

    // Firebase Realtime Database references
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("user");

    // essential URL structure is built using constants
    public static final String ACCESS_KEY = "347b7abc9391d65585b6aa71c3e61577";
    public static final String BASE_URL = "http://apilayer.net/api/";
    public static final String ENDPOINT = "live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Check if Google Play Services are available
        if (checkPlayServices()) {
            Log.d("Success","Play Services Available");
        } else {
            Toast.makeText(this, "Play Services NOT Available", Toast.LENGTH_LONG).show();
        }

        checkPermissions();

        // Create a Google API Client if one does not already exist
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.d("Success","Google API Client Created");
        }

        // User Object passed from LoginActivity, user info displayed in text view
        passedUser = (User) getIntent().getSerializableExtra("user");
        textView = (TextView) this.findViewById(R.id.userInfo);
        ((TextView) textView).setText("User Name: " + passedUser.getName() + "\nCountry: " + passedUser.getCountry()
                + "\nTap Photo to change Profile Picture");

        // Required for accessing external storage, taken from stackoverflow, will prompt user for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Log.d("Success","Have Desired Permissions");
        }

        // When image view is clicked local camera application is invoked
        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File gallery = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                image = new File(gallery, passedUser.getName() + "profilePic.jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                setResult(Activity.RESULT_OK, intent);
                startActivityForResult(intent, PHOTO_TAKEN);
            }
        });

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


        // Check network connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            Log.d("Success","Successful Connection to Network");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No Connection!", Toast.LENGTH_LONG).show();
            ((EditText) findViewById(R.id.edit)).setText("No Internet Connection");
        }

        editText = (EditText)findViewById(R.id.enterDestination);

        Button search = (Button)findViewById(R.id.flightBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                String keyword= "Flights " + passedUser.getCountry() + " to " + destCountry.getName();
                intent.putExtra(SearchManager.QUERY, keyword);
                startActivity(intent);
            }
        });

        Button exchange = (Button) findViewById(R.id.exchangeBtn);
            exchange.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    editText.setError(null);
                    if(TextUtils.isEmpty(editText.getText())){
                        editText.requestFocus();
                        editText.setError("No Destination Entered!");
                    } else {

                    new AsyncTask<Void, Void, String>() {
                        protected String doInBackground(Void... params) {
                            try {
                                return jsonToString();
                            } catch (Exception e) {
                            }
                            return "";
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            try {
                                findViewById(R.id.flightBtn).setVisibility(View.VISIBLE);
                                findViewById(R.id.mapBtn).setVisibility(View.VISIBLE);
                                findViewById(R.id.infoBtn).setVisibility(View.VISIBLE);
                                findViewById(R.id.currBtn).setVisibility(View.VISIBLE);
                                JSONArray items = (JSONArray) (new JSONTokener(result).nextValue());
                                Country c = new Country();
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = (JSONObject) items.get(i);
                                    c.setName(item.getString("name"));
                                    c.setCapital(item.getString("capital"));
                                    c.setRegion(item.getString("region"));
                                    c.setDemonym(item.getString("demonym"));
                                    c.setCurrencies(item.getString("currencies"));
                                    c.setLanguages(item.getString("languages"));
                                    String loc = item.getString("latlng");
                                    String lat = loc.substring(1, loc.indexOf(","));
                                    String lng = loc.substring(loc.indexOf(",") + 1, loc.length() - 1);
                                    c.setLat(Double.valueOf(lat));
                                    c.setLng(Double.valueOf(lng));

                                    try {
                                        JSONArray currencies = (JSONArray) (new JSONTokener(c.getCurrencies()).nextValue());
                                        for (int j = 0; j < currencies.length(); j++) {
                                            JSONObject curr = (JSONObject) currencies.get(j);
                                            c.setCurrencies(curr.getString("code"));
                                        }
                                    } catch (Exception e) {
                                    }

                                    try {
                                        JSONArray languages = (JSONArray) (new JSONTokener(c.getLanguages()).nextValue());
                                        for (int k = 0; k < languages.length(); k++) {
                                            JSONObject curr = (JSONObject) languages.get(k);
                                            c.setLanguages(curr.getString("name"));
                                        }
                                    } catch (Exception e) {
                                    }
                                    destCountry = c;
                                }
                            } catch (Exception e) {
                                findViewById(R.id.flightBtn).setVisibility(View.INVISIBLE);
                                findViewById(R.id.mapBtn).setVisibility(View.INVISIBLE);
                                findViewById(R.id.infoBtn).setVisibility(View.INVISIBLE);
                                findViewById(R.id.currBtn).setVisibility(View.INVISIBLE);
                                editText.requestFocus();
                                editText.setError("Invalid Destination!");
                                e.printStackTrace();
                            }
                        }
                    }.execute();}
                }
            });

        // Action taken when device is shaken
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeListener();
        mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {

            public void onShake() {
                Toast.makeText(UserHomeActivity.this, "Logout!", Toast.LENGTH_SHORT).show();
                logout();
            }
        });
    }

    // When app starts connection is made to Google API via the client instance
    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    // When App is stopped connection to Google API is closed
    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    // When App resumes from paused state sensor listener is re-registered
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    // When App is paused sensor listener is unregistered
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    // This method is invoked by sensors
    public void logout(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Check if storage can be written to/read from
    public boolean checkPermissions() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        Toast.makeText(this, "Not Writable/Readable", Toast.LENGTH_LONG).show();
        return false;
    }

    // This is a Sub-Activity it takes over when the Camera Intent is finished
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
        } catch (NullPointerException e) {
            Toast.makeText(this, "Issue Saving Photo, Try Again", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            recreate();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /*class MyAsyncTask extends AsyncTask {

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
    }*/

    public void setResult(String result) {
        this.result = result;
    }

    /*public void selectFrag(View view) {
        // MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);
        //mp.start();
        Fragment fr = null;

        *//*if(view == findViewById(R.id.button2)) {
            fr = new FragmentTwo();

        }else {
            fr = new FragmentOne();
        }*//*

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, fr);
        fragmentTransaction.commit();
    }*/

    // Gets JSON data from Web Service and converts it into a string
    private String jsonToString() throws Exception {
        if(editText.getText().toString() == null) {
            editText.setError("No Destination Entered");
            editText.requestFocus();
            return "";
        }

        URL url = new URL("https://restcountries.eu/rest/v2/name/"+editText.getText().toString());
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = "";

        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    // This Method checks play services, taken from Google Docs
    public boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(this, result, 2404).show();
            }
            return false;
        }
        return true;
    }

    // Invokes Google Maps
    protected void showMap(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("destCountry", destCountry);
        intent.putExtra("user", passedUser);
        startActivity(intent);
    }

    protected void destinationInfo(View view){
        Intent intent = new Intent(this, DestinationInfoActivity.class);
        intent.putExtra("destCountry", destCountry);
        //intent.putExtra("user", passedUser);
        startActivity(intent);
    }

    protected void currencyConverter(View view){
        Intent intent = new Intent(this, CurrencyConverterActivity.class);
        intent.putExtra("destCountry", destCountry);
        //intent.putExtra("user", passedUser);
        startActivity(intent);
    }

}
