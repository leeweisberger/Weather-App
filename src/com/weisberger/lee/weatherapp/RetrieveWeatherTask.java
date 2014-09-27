package com.weisberger.lee.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;

class RetrieveWeatherTask extends AsyncTask<String, Void, JSONObject> {
	@Override
	protected JSONObject doInBackground(String... params) {
		JSONObject jsonResult=null;
		try {
			URL obj = new URL(params[0]);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONTokener tokener = new JSONTokener(response.toString());
			try {
				jsonResult = new JSONObject(tokener);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(jsonResult.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}


		return jsonResult;
	}
}