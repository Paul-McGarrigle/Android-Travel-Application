package com.example.paulm.travelapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import models.Country;

public class DestinationInfoActivity extends AppCompatActivity {

    private Country destCountry;
    private TextView name, capital, region, nationality, currency, language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_info);
        destCountry = (Country) getIntent().getSerializableExtra("destCountry");
        name = (TextView)findViewById(R.id.name);
        capital = (TextView)findViewById(R.id.capital);
        region = (TextView)findViewById(R.id.region);
        nationality = (TextView)findViewById(R.id.nationality);
        currency = (TextView)findViewById(R.id.currency);
        language = (TextView)findViewById(R.id.language);

        name.setText("Name: " + destCountry.getName());
        capital.setText("Capital: " + destCountry.getCapital());
        region.setText("Region: " + destCountry.getRegion());
        nationality.setText("Nationality: " + destCountry.getDemonym());
        currency.setText("Currency: " + destCountry.getCurrencies());
        language.setText("Language: " + destCountry.getLanguages());
    }

}
