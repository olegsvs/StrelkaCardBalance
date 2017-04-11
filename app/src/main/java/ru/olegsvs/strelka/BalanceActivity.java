package ru.olegsvs.strelka;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class BalanceActivity extends AppCompatActivity {
    private TextView balance;
    private EditText edStrelkaId;
    private Button btCheckIt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        edStrelkaId = (EditText) findViewById(R.id.edStrelkaId);
        balance = (TextView) findViewById(R.id.tvBalance);
        btCheckIt = (Button) findViewById(R.id.btCheckIt);
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        layout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                        getValues(null);
                    }
                }, 500);
            }
        });

        ScrollView view = (ScrollView)findViewById(R.id.scView);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("StrelkaIDs", Context.MODE_PRIVATE);
        if (sharedPref.contains("ID")) {
            edStrelkaId.setText(sharedPref.getString("ID", ""));
            balance.setText(R.string.pressMe);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_balance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getValues(View v) {
        if (edStrelkaId.length() < 11) {
            balance.setText(R.string.hint_11dg);
            return;
        }
        SharedPreferences sharedPref = getSharedPreferences("StrelkaIDs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ID", edStrelkaId.getText().toString());
        editor.apply();

        BalanceTask bt = new BalanceTask();
        bt.execute();
    }

    private class BalanceTask extends AsyncTask< Void, Void, String > {
        private String result, ID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            balance.setText(R.string.loading);
            btCheckIt.setEnabled(false);
            ID = edStrelkaId.getText().toString();
        }

        @Override
        protected String doInBackground(Void[] p1) {
            try {
                URL strelka = new URL("https://strelkacard.ru/api/cards/status/?cardnum=" + ID + "&cardtypeid=3ae427a1-0f17-4524-acb1-a3f50090a8f3");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(strelka.openStream()));

                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in .readLine()) != null)
                    sb.append(inputLine); in .close();
                if (isJSONValid(sb.toString())) {
                    JSONObject strelkaJSON = new JSONObject(sb.toString());
                    result = (getString(R.string.prBalance) + Double.parseDouble(strelkaJSON.getString("balance")) / 100 + "\u20BD");
                } else
                    result = getString(R.string.jsonError);
            } catch (Exception e) {
                result = getString(R.string.Error);
                if (e.toString().contains("FileNotFound"))
                    Snackbar.make(getWindow().findViewById(android.R.id.content), getString(R.string.IDNotFound), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (e.toString().contains("UnknownHost"))
                    Snackbar.make(getWindow().findViewById(android.R.id.content), getString(R.string.internetError), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            return (result != null) ? result : getString(R.string.UnknownError);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            balance.setText(result);
            btCheckIt.setEnabled(true);
        }

        private boolean isJSONValid(String test) {
            try {
                new JSONObject(test).getString("balance");
            } catch (JSONException ex) {
                return false;
            }
            return true;
        }
    }
}
