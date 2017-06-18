package com.example.paulm.travelapp;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import models.Country;

// ListView example taken from https://www.mkyong.com/android/android-listview-example/
public class DestinationInfoActivity extends ListActivity {

    private Country destCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        destCountry = (Country) getIntent().getSerializableExtra("destCountry");

        final String[] INFO = new String[] {
                "Destination Name: \n" + destCountry.getName(),
                "Capital City: \n" + destCountry.getCapital(),
                "World Region: \n" + destCountry.getRegion(),
                "Nationality: \n" + destCountry.getDemonym(),
                "Local Currency: \n" + destCountry.getCurrencies(),
                "Spoken Language: \n" + destCountry.getLanguages()};

        setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_destination_info, INFO));

        ListView listView = getListView();
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),((TextView) view).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
