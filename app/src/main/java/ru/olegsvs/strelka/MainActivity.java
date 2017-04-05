package ru.olegsvs.strelka;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONException;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.annotation.*;

public class MainActivity extends Activity {
    private TextView balance;
    private EditText edStrelkaId;
    private Button btCheckIt;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
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
					getValues(null);
					layout.setRefreshing(false);
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

    private class BalanceTask extends AsyncTask < Void, Void, String > {
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
                if (e.toString().contains("FileNotFound"))
                    result = getString(R.string.IDNotFound);
                if (e.toString().contains("UnknownHost"))
                    result = getString(R.string.internetError);
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
