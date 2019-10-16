
package com.ashish.securepreferences.sample.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;

public class SecureSharedPreferences implements SharedPreferences
{
	private Context mContext;
	private SharedPreferences mPreferences;
	private SecureSharedPrefsCrypto mPrefsCrypto;

	/**
	 * @param context
	 * @param preferences
	 * @param encryptionKey
	 */
	public SecureSharedPreferences(@NonNull Context context, @NonNull final SharedPreferences preferences, @NonNull String encryptionKey) throws Exception
	{
		mContext = context;
		mPreferences = preferences;
		mPrefsCrypto = new SecureSharedPrefsCrypto(context, encryptionKey);
	}

	public Editor edit()
	{
		return new Editor();
	}

	public SharedPreferences getCoreSharedPreferences()
	{
		return mPreferences;
	}

	/**
	 * returns a map of all the encrypted key, unencrypted value pairs
	 *
	 * @return
	 */
	@Override
	public Map<String, String> getAll()
	{
		Map<String, ?> all = mPreferences.getAll();
		Set<String> keys = all.keySet();
		HashMap<String, String> unencryptedMap = new HashMap<>(keys.size());
		for (String key : keys)
		{
			Object value = all.get(key);
			if (value != null)
			{
				unencryptedMap.put(key, decrypt(value.toString()));
			}
		}
		return unencryptedMap;
	}

	@Override
	public boolean getBoolean(String key, boolean defValue)
	{
		final String v = mPreferences.getString(encryptKey(key), null);
		final String dv = decrypt(v);
		return SecureSharedPrefsCrypto.isValidString(dv) ? Boolean.parseBoolean(dv) : defValue;
	}

	@Override
	public float getFloat(String key, float defValue)
	{
		final String v = mPreferences.getString(encryptKey(key), null);
		final String dv = decrypt(v);
		return SecureSharedPrefsCrypto.isValidString(dv) ? Float.parseFloat(dv) : defValue;
	}

	@Override
	public int getInt(String key, int defValue)
	{
		final String v = mPreferences.getString(encryptKey(key), null);
		final String dv = decrypt(v);
		return SecureSharedPrefsCrypto.isValidString(dv) ? Integer.parseInt(dv) : defValue;
	}

	@Override
	public long getLong(String key, long defValue)
	{
		final String v = mPreferences.getString(encryptKey(key), null);
		final String dv = decrypt(v);
		return SecureSharedPrefsCrypto.isValidString(dv) ? Long.parseLong(dv) : defValue;
	}

	@Override
	public String getString(String key, String defValue)
	{
		final String v = mPreferences.getString(encryptKey(key), null);
		final String dv = decrypt(v);
		return SecureSharedPrefsCrypto.isValidString(dv) ? dv : defValue;
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValues)
	{
		final Set<String> stringSet = mPreferences.getStringSet(encryptKey(key), defValues);
		return stringSet != null ? decryptSet(stringSet) : defValues;
	}

	@Override
	public boolean contains(String s)
	{
		s = encryptKey(s);
		return mPreferences.contains(s);
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
	{
		mPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener)
	{
		mPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	private String encryptKey(String key)
	{
		return mPrefsCrypto.hashPrefKey(key);
	}

	protected String encrypt(String value)
	{
		return mPrefsCrypto.encrypt(value);
	}

	protected String decrypt(String value)
	{
		return mPrefsCrypto.decrypt(value);
	}

	private Set<String> encryptSet(Set<String> values)
	{
		Set<String> encryptedValues = new HashSet<String>();
		for (String value : values)
		{
			encryptedValues.add(encrypt(value));
		}
		return encryptedValues;
	}

	private Set<String> decryptSet(Set<String> values)
	{
		Set<String> decryptedValues = new HashSet<String>();
		for (String value : values)
		{
			decryptedValues.add(decrypt(value));
		}
		return decryptedValues;
	}

	public class Editor implements SharedPreferences.Editor
	{

		protected SharedPreferences.Editor mPreferencesEditor;

		public Editor()
		{
			this.mPreferencesEditor = SecureSharedPreferences.this.mPreferences.edit();
		}

		@Override
		public Editor putBoolean(String key, boolean value)
		{
			mPreferencesEditor.putString(encryptKey(key), encrypt(Boolean.toString(value)));
			return this;
		}

		@Override
		public Editor putFloat(String key, float value)
		{
			mPreferencesEditor.putString(encryptKey(key), encrypt(Float.toString(value)));
			return this;
		}

		@Override
		public Editor putInt(String key, int value)
		{
			mPreferencesEditor.putString(encryptKey(key), encrypt(Integer.toString(value)));
			return this;
		}

		@Override
		public Editor putLong(String key, long value)
		{
			mPreferencesEditor.putString(encryptKey(key), encrypt(Long.toString(value)));
			return this;
		}

		@Override
		public Editor putString(String key, String value)
		{
			mPreferencesEditor.putString(encryptKey(key), encrypt(value));
			return this;
		}

		public Editor putStringWithoutEncryptKey(String key, String value)
		{
			mPreferencesEditor.putString(key, encrypt(value));
			return this;
		}

		@Override
		public SharedPreferences.Editor putStringSet(String key, Set<String> values)
		{
			mPreferencesEditor.putStringSet(encryptKey(key), encryptSet(values));
			return this;
		}

		@Override
		public void apply()
		{
			mPreferencesEditor.apply();
		}

		@Override
		public Editor clear()
		{
			mPreferencesEditor.clear();
			return this;
		}

		@Override
		public boolean commit()
		{
			return mPreferencesEditor.commit();
		}

		@Override
		public Editor remove(String s)
		{
			mPreferencesEditor.remove(encryptKey(s));
			return this;
		}
	}
}