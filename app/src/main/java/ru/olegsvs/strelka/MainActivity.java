package ru.olegsvs.strelka;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import ru.olegsvs.strelka.*;

public class MainActivity extends Activity {
 private TextView balance;
 private Button btCheckIt;
 private EditText edStrelkaId;
 
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);
  edStrelkaId = (EditText) findViewById(R.id.edStrelkaId);
  balance = (TextView) findViewById(R.id.tvBalance);
  balance.setText("");
  SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
  if (sharedPref.contains("StrelkaIds")) {
   edStrelkaId.setText(sharedPref.getString("StrelkaIds", ""));
  }
  StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
  StrictMode.setThreadPolicy(policy);

 }
 public void getValues(View v) {
  if (edStrelkaId.length() < 11) {
   balance.setText(R.string.hint_8dg);
   return;
  }
  SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
  SharedPreferences.Editor editor = sharedPref.edit();
  editor.putString("StrelkaIds", edStrelkaId.getText().toString());
  editor.commit();
  try {
   getBalance();
  } catch (Exception e) {
   if (e.toString().contains("FileNotFound"))
    balance.setText(R.string.IdNotFnd);
   if (e.toString().contains("UnknownHost"))
    balance.setText(R.string.internetErr);

  }
 }

 public void getBalance() throws Exception {
  URL strelka = new URL("http://strelkacard.ru/api/cards/status/?cardnum=" + edStrelkaId.getText().toString() + "&cardtypeid=3ae427a1-0f17-4524-acb1-a3f50090a8f3");
  BufferedReader in = new BufferedReader(
   new InputStreamReader(strelka.openStream()));

  String inputLine;
  StringBuilder sb = new StringBuilder();

  while ((inputLine = in .readLine()) != null)
   sb.append(inputLine); in .close();
  Integer a = sb.indexOf("balance") + 9;
  Integer b = sb.indexOf("baserate") - 2;
  balance.setText(getString(R.string.prBalance) + Double.parseDouble(sb.substring(a, b)) / 100 + "p.");
 }
}
