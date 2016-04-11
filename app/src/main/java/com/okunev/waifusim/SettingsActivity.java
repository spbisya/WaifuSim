package com.okunev.waifusim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gwa on 4/11/16.
 */
public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sPref;
    Button getNewToken, save, changeToken;
    EditText curToken;
TextView or;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_settings);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        curToken = (EditText) findViewById(R.id.curToken);
        or = (TextView)findViewById(R.id.textView5);
        curToken.setText(sPref.getString("token", ""));
        curToken.setEnabled(false);
        getNewToken = (Button) findViewById(R.id.getNewToken);
        changeToken = (Button) findViewById(R.id.changeToken);
        save = (Button) findViewById(R.id.saveCurToken);

        getNewToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        changeToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToken.setVisibility(View.INVISIBLE);
                curToken.setEnabled(true);
                or.setVisibility(View.INVISIBLE);
                getNewToken.setVisibility(View.INVISIBLE);
                save.setVisibility(View.VISIBLE);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(SettingsActivity.this, "Saved!",Toast.LENGTH_LONG).show();
                        sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString("token", curToken.getText().toString());
                        ed.commit();
                        finish();
                        if(getIntent().getBooleanExtra("new",false)){
                            Intent intent = new Intent( SettingsActivity.this, MainActivity.class);

                            startActivity(intent);
                        }
                    }
                });
            }
        });

    }

    public void createUser() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-52-38-11-210.us-west-2.compute.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WaifuApi waifuApi = retrofit.create(WaifuApi.class);
        Call<Token> call = waifuApi.user();
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Toast.makeText(SettingsActivity.this, "Token "+response.body().toString()+" generated!",Toast.LENGTH_LONG).show();
                curToken.setText(response.body().toString());
                sPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("token", response.body().toString());
                ed.commit();
                finish();
                if(getIntent().getBooleanExtra("new",false)){
                    Intent intent = new Intent( SettingsActivity.this, MainActivity.class);

                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

            }
        });
    }
}
