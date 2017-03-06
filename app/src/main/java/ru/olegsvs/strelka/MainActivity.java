package ru.olegsvs.strelka;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import ru.olegsvs.strelka.*;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class MainActivity extends Activity
{
	private TextView balance;
	private EditText edStrelkaId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		edStrelkaId = (EditText) findViewById(R.id.edStrelkaId);
		balance = (TextView) findViewById(R.id.tvBalance);
		balance.setText("");
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		if (sharedPref.contains("StrelkaIds"))
		{
			edStrelkaId.setText(sharedPref.getString("StrelkaIds", ""));
		}
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
		StrictMode.setThreadPolicy(policy);

	}
	public void getValues(View v)
	{
		if (edStrelkaId.length() < 11)
		{
			balance.setText(R.string.hint_11dg);
			return;
		}
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("StrelkaIds", edStrelkaId.getText().toString());
		editor.commit();
		try
		{
			getBalance();
		}
		catch (Exception e)
		{
			if (e.toString().contains("FileNotFound"))
				balance.setText(R.string.IdNotFnd);
			if (e.toString().contains("UnknownHost"))
				balance.setText(R.string.internetErr);

		}
	}

	public void getBalance() throws Exception
	{
		URL strelka = new URL("http://strelkacard.ru/api/cards/status/?cardnum=" + edStrelkaId.getText().toString() + "&cardtypeid=3ae427a1-0f17-4524-acb1-a3f50090a8f3");
		BufferedReader in = new BufferedReader(
			new InputStreamReader(strelka.openStream()));

		String inputLine;
		StringBuilder sb = new StringBuilder();

		while ((inputLine = in .readLine()) != null)
			sb.append(inputLine); in .close();
		if (isJSONValid(sb.toString()))
		{
			JSONObject strelkaJSON = new JSONObject(sb.toString());
			balance.setText(getString(R.string.prBalance) + Double.parseDouble(strelkaJSON.getString("balance")) / 100 + "\u20BD");
		}
		else
			balance.setText(R.string.jsonErr);
	}

	public boolean isJSONValid(String test)
	{
		try
		{
			new JSONObject(test);
		}
		catch (JSONException ex)
		{
			try
			{
				new JSONArray(test);
			}
			catch (JSONException ex1)
			{
				return false;
			}
		}
		return true;
	}
}
