package com.weisberger.lee.weatherapp;

import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

class RetrieveWeatherIconTask extends AsyncTask<String, Void, Bitmap> {
	@Override
	protected Bitmap doInBackground(String... params) {
		URL url;
		Bitmap bmp=null;
		try {
			url = new URL("http://openweathermap.org/img/w/" + params[0] + ".png");
			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap resized = Bitmap.createScaledBitmap(bmp, 200, 200, true);
		return resized;
	}
}