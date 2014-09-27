package com.weisberger.lee.weatherapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainFragment extends Fragment {
	private EditText mLocation;
	private JSONObject mCurrentJSONWeather;
	private JSONObject mProjectedJSONWeather;
	private LinearLayout mCurrentConditionsLayout;
	private View mView;
	private LinearLayout mProjectedConditionsLayout;
	private static final int FORECAST_LENGTH=3;

	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_main, container, false);
		mLocation = (EditText) mView.findViewById(R.id.location);
		Button submitWeather = (Button) mView.findViewById(R.id.submit_button);
		submitWeather.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					mCurrentJSONWeather = new RetrieveWeatherTask().execute(
							"http://api.openweathermap.org/data/2.5/weather?units=imperial&q="+mLocation.getText().toString()).get();
					mProjectedJSONWeather = new RetrieveWeatherTask().execute(
							"http://api.openweathermap.org/data/2.5/forecast/daily?units=imperial&cnt=3&mode=json&q="+mLocation.getText().toString()).get();
					updateWeather();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		final LocationManager locationManager = (LocationManager)
				getActivity().getSystemService(Context.LOCATION_SERVICE);
		ImageButton gpsButton = (ImageButton) mView.findViewById(R.id.gps);
		gpsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLocation.setText(getCity(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)));

			}
		});
		mCurrentConditionsLayout = (LinearLayout) mView.findViewById(R.id.current_conditions_layout);
		mProjectedConditionsLayout = (LinearLayout) mView.findViewById(R.id.forecast_layout);
		return mView;
	}

	private void updateWeather() throws JSONException, InterruptedException, ExecutionException {
		mCurrentConditionsLayout.removeAllViews();
		mProjectedConditionsLayout.removeAllViews();
		updateCurrentWeather();
		updateProjectedWeather();
	}

	private void updateCurrentWeather()
			throws JSONException, InterruptedException, ExecutionException {
		Map<String, String> conditions = extractCurrentFields(mCurrentJSONWeather);
		((TextView)mView.findViewById(R.id.temperature)).setText(mCurrentJSONWeather.getJSONObject("main").getString("temp")+"\u2109");
		ImageView weatherPic = (ImageView) mView.findViewById(R.id.current_weather_pic);
		weatherPic.setImageBitmap(new RetrieveWeatherIconTask().execute(mCurrentJSONWeather.getJSONArray("weather").getJSONObject(0).getString("icon")).get());
		for(String condition: conditions.keySet()){
			if(condition.equals("Temp:"))
				continue;
			TextView label = new TextView(getActivity());
			label.setText(condition);
			label.setTextSize(16);
			TextView value = new TextView(getActivity());
			value.setText(conditions.get(condition));
			value.setTextSize(16);
			LinearLayout ll = initializeLinearLayout(LinearLayout.HORIZONTAL);
			ll.addView(label);
			ll.addView(value);
			mCurrentConditionsLayout.addView(ll);
		}
	}

	private void updateProjectedWeather() throws InterruptedException, ExecutionException {
		for(int i=0; i<FORECAST_LENGTH; i++){
			try {
				JSONArray jsonForecast = mProjectedJSONWeather.getJSONArray("list");
				Map<String, String> conditions = extractProjectedFields(jsonForecast.getJSONObject(i));
				LinearLayout dayLayout = initializeLinearLayout(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)dayLayout.getLayoutParams();
				params.setMargins(12, 12, 12, 32);
				dayLayout.setLayoutParams(params);
				dayLayout.addView(getDate(i));

				for(String condition: conditions.keySet()){
					LinearLayout ll = initializeLinearLayout(LinearLayout.HORIZONTAL);
					if(condition.equals("Icon:")){
						ImageView icon = new ImageView(getActivity());
						icon.setImageBitmap(new RetrieveWeatherIconTask().execute(
								conditions.get("Icon:")).get());
						ll.addView(icon);
					}
					else{
						TextView label = new TextView(getActivity());
						label.setText(condition);
						label.setTextSize(12);
						TextView value = new TextView(getActivity());
						value.setText(conditions.get(condition));
						value.setTextSize(12);
						ll.addView(label);
						ll.addView(value);
					}
					dayLayout.addView(ll);

				}
				mProjectedConditionsLayout.addView(dayLayout);
				if(i!=FORECAST_LENGTH-1)
					mProjectedConditionsLayout.addView(makeVerticalLine());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private LinearLayout initializeLinearLayout(int orientation) {
		LinearLayout ll = new LinearLayout(getActivity());
		ll.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setOrientation(orientation);
		return ll;
	}

	private Map<String, String> extractCurrentFields(JSONObject jsonWeather) throws JSONException {
		Map<String,String> conditions = new LinkedHashMap<String,String>();
		conditions.put("Temp:", jsonWeather.getJSONObject("main").getString("temp")+"\u2109");
		conditions.put("Min:",jsonWeather.getJSONObject("main").getString("temp_min")+"\u2109");
		conditions.put("Max:",jsonWeather.getJSONObject("main").getString("temp_max")+"\u2109");
		conditions.put("Humidity:",jsonWeather.getJSONObject("main").getString("humidity") + "%");
		conditions.put("Wind:",jsonWeather.getJSONObject("wind").getString("speed") + " mph");
		conditions.put("Descr:",jsonWeather.getJSONArray("weather").getJSONObject(0).getString("description"));
		return conditions;
	}

	private Map<String, String> extractProjectedFields(JSONObject jsonWeather) throws JSONException {
		Map<String,String> conditions = new LinkedHashMap<String,String>();
		conditions.put("Icon:",jsonWeather.getJSONArray("weather").getJSONObject(0).getString("icon"));
		conditions.put("Descr:",jsonWeather.getJSONArray("weather").getJSONObject(0).getString("description"));
		conditions.put("Min:",jsonWeather.getJSONObject("temp").getString("min")+"\u2109");
		conditions.put("Max:",jsonWeather.getJSONObject("temp").getString("max")+"\u2109");
		conditions.put("Humidity:",jsonWeather.getString("humidity") + "%");
		conditions.put("Wind:",jsonWeather.getString("speed") + " mph");
		return conditions;
	}

	private View makeVerticalLine() {
		View verticalLine = new View(getActivity());
		verticalLine.setLayoutParams(new LinearLayout.LayoutParams(
				1, LinearLayout.LayoutParams.FILL_PARENT));
		verticalLine.setBackgroundColor(Color.rgb(51, 51, 51));
		return verticalLine;
	}

	private TextView getDate(int i) {
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, i+1);
		TextView formattedDate = new TextView(getActivity());
		formattedDate.setText(df.format(c.getTime()));
		return formattedDate;
	}

	private String getCity(Location location){
		String city="";
		Geocoder gcd = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());
		List<Address> addresses;
		try {
			addresses = gcd.getFromLocation(location.getLatitude(),
					location.getLongitude(), 1);
			city = addresses.get(0).getLocality();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return city;
	}
}
