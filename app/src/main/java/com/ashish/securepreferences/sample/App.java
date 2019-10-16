package com.ashish.securepreferences.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ashish.securepreferences.sample.preferences.SecureSharedPreferences;

import hugo.weaving.DebugLog;

/**
 * Sample app
 */
public class App extends Application
{
	private static final String TAG = "secureprefsample";

	protected static App instance;
	private SecureSharedPreferences mSecurePrefs;

	public App()
	{
		super();
		instance = this;
	}

	public static App get()
	{
		return instance;
	}

	/**
	 * Single point for the app to get the secure prefs object
	 *
	 * @return
	 */
	@DebugLog
	public SecureSharedPreferences getSharedPreferences()
	{
		if (mSecurePrefs == null)
		{
			try
			{
				mSecurePrefs = getNewSecurePreferences(this, "my_prefs");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Log.e(TAG, "getSharedPreferences() - ", ex);
			}
		}
		return mSecurePrefs;
	}

	private SecureSharedPreferences getNewSecurePreferences(Context context, String fileName) throws Exception
	{
		final String sharedPrefFileName = context.getPackageName() + "_" + fileName;

		SharedPreferences sharedPrefDelegate = context.getSharedPreferences(sharedPrefFileName, Context.MODE_PRIVATE);
		return new SecureSharedPreferences(context, sharedPrefDelegate, "TEST-123");
	}
}
