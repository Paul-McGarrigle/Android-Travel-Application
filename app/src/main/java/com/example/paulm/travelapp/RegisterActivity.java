package com.example.paulm.travelapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

public class RegisterActivity extends Activity {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView, mNameView, mCountryView;
    private View mProgressView, mLoginFormView;
    private String mName, mEmail, mPassword, mCountry;
    private ArrayList<User> userList = new ArrayList<User>();
    private boolean emailInUse = false;
    private String mImage = "";

    // Firebase Realtime Database references
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("user");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mNameView = (EditText) findViewById(R.id.name);
        mCountryView = (EditText) findViewById(R.id.country);
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
        Button mRegButton = (Button) findViewById(R.id.reg_button);
        mRegButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptReg();
            }
        });
    }

    // Invoked when Register button is clicked
    private void attemptReg() {
        // Reset errors, if there are any previous errors
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);
        mCountryView.setError(null);

        // Store values at the time of the register attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String name = mNameView.getText().toString();
        String country = mCountryView.getText().toString();

        // Variables for validation of inputted data
        boolean cancel = false;
        View focusView = null;

        // Check if email is in use
        for(User u: userList){
            if(u.getEmail().equalsIgnoreCase(email)) {
                emailInUse = true;
            }
        }

        // Validates supplied registration data
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(country)) {
            mCountryView.setError(getString(R.string.error_field_required));
            focusView = mCountryView;
            cancel = true;
        } else if (emailInUse) {
            mEmailView.setError(getString(R.string.duplicate_email));
            focusView = mEmailView;
            cancel = true;
            emailInUse = false;
            focusView.requestFocus();
        }

        if (cancel) {
            // On error register process is not completed and focus returns to first area of error
            focusView.requestFocus();
        } else {
            // Show loading screen for two seconds before createAccount Method is invoked
            showProgress(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createAccount();
                }
            }, 2000);
        }

    }

    // Invoked when user data passes validation process
    private void createAccount(){
        // Get values in Views
        mName = ((EditText) findViewById(R.id.name)).getText().toString();
        mEmail = ((EditText) findViewById(R.id.email)).getText().toString();
        mPassword = ((EditText) findViewById(R.id.password)).getText().toString();
        mCountry = ((EditText) findViewById(R.id.country)).getText().toString();

        // Create User instance based on validated data
        User user = new User(mName, mEmail, mPassword, mCountry, mImage);

        // Specify unique value, i.e. Primary Key, for User record and add this user to Firebase Database
        String key = mUserRef.push().getKey();
        String userName = mEmail.substring(0, mEmail.indexOf("."));
        mRootRef.child(userName).setValue(user);

        // Go to Login page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Check if email is valid, must be in format digits@digits.digits,
    // digits after . must be greater than 2 characters wide
    private boolean isEmailValid(String email) {
        String pattern = "^[_A-Za-z0-9-\\+]*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if(email.matches(pattern)){
            return true;
        } else {
            return false;
        }
    }

    // Check if password is valid, i.e must be greater than 6 characters
    // & contain upper case, lower case & numeric characters, Regular Expression
    // from https://www.mkyong.com/regular-expressions
    private boolean isPasswordValid(String password) {
        String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,})";
        if(password.matches(pattern)){
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
}