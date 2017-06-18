package com.example.paulm.travelapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import models.Country;

public class CurrencyConverterActivity extends AppCompatActivity {

    private Country destCountry;
    private String exchangeRate;
    private EditText inputView;
    private double input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        destCountry = (Country) getIntent().getSerializableExtra("destCountry");
        inputView = (EditText)findViewById(R.id.cashIn);


        final Button exchange = (Button) findViewById(R.id.convertBtn);
        exchange.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
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
                                JSONObject item = (JSONObject) (new JSONTokener(result).nextValue());
                                exchangeRate = item.getString("rates");

                                EditText e = (EditText)findViewById(R.id.cashOut);
                                String s = exchangeRate.substring(exchangeRate.indexOf(":")+1, exchangeRate.length()-1);
                                double rate = Double.valueOf(s);

                                if(inputView.getText().toString() == null || inputView.getText().toString().isEmpty()){
                                    input = 0.0;
                                } else {
                                    input = Double.parseDouble(inputView.getText().toString());
                                }
                                double output = rate * input;
                                e.setText(destCountry.getCurrencies()+input+" at a rate of " + rate + " will return €"+output);
                                Toast.makeText(CurrencyConverterActivity.this, "HERE", Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                inputView.requestFocus();
                                inputView.setError("Invalid Amount Entered!");
                                e.printStackTrace();
                            }
                        }
                    }.execute();}
        });
    }

    private String jsonToString() throws Exception {
        URL url = new URL("http://api.fixer.io/latest?symbols=" + destCountry.getCurrencies());
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
}
