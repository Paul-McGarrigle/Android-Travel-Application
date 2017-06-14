package com.example.paulm.travelapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import models.User;

import static android.Manifest.permission.READ_CONTACTS;


public class LoginActivity extends Activity {

    // Variable to identify the requesting of priviledges on initial startup of application
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView, mLoginFormView;
    private String mName, mEmail;
    private ArrayList<User> userList = new ArrayList<User>();
    private boolean emailInUse = false, validLogin = false;
    private User passUser;

    // Firebase Realtime Database references
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("user");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

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

        // Set up button listener
        Button mLoginButton = (Button) findViewById(R.id.email_sign_in_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    // Invoked when Login button is clicked
    private void attemptLogin() {
        // Reset errors, if there are any previous errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        // Variables for validation of inputted data
        boolean cancel = false;
        View focusView = null;

        // Check if email is valid
        for(User u: userList){
            if(u.getEmail().equalsIgnoreCase(email)) {
                emailInUse = true;
                // If email is valid check Method is invoked to validate password
                if(check(u, password)){
                    validLogin = true;
                    passUser = u;
                } else {
                    validLogin = false;
                }
            }
        }


        // Validates supplied login data
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!emailInUse) {
            mEmailView.setError(getString(R.string.error_email_not_in_use));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!validLogin) {
            mPasswordView.setError(getString(R.string.error_wrong_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // On error login process is not completed and focus returns to first area of error
            focusView.requestFocus();
        } else {
            // Show loading screen for two seconds before login Method is invoked
            showProgress(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    login(passUser);
                }
            }, 2000);
        }

    }

    // If login credentials are met this Method is invoked
    private void login(User user){
        // Go to user homapage and pass User Object
        Intent intent = new Intent(this, UserHomeActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    // Invoked when register link is invoked
    protected void register(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // Checks if specified password matches the stored password linked with that email address/account
    private boolean check(User user, String password){
        if(user.getPassword().equals(password)) {
            return true;
        } else {
            return false;
        }
    }

    // Shows the loading screen progress bar.  This Method is taken from built in Android Studio
    // Login Activity
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // This Method asks user for permissions on initial start up of the application.
    // This Method is taken from built in Android Studio Login Activity
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

}

