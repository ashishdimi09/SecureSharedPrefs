
package com.ashish.securepreferences.sample.preferences;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import androidx.annotation.NonNull;

public class SecureSharedPrefsCrypto
{
	private static final String TAG = "SecurePrefsCrypto";

	private Context mContext;
	private AesCbcWithIntegrity.SecretKeys mSecretKeys;

	public SecureSharedPrefsCrypto(Context context, @NonNull String encryptionKey) throws GeneralSecurityException
	{
		mContext = context;
		mSecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(encryptionKey, getSalt());
	}

	/**
	 * The Pref keys must be same each time so we're using a hash to obscure the stored value
	 *
	 * @param prefKey
	 * @return SHA-256 Hash of the preference key
	 */
	public static String hashPrefKey(String prefKey)
	{
		final MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = prefKey.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);

			return Base64.encodeToString(digest.digest(), AesCbcWithIntegrity.BASE64_FLAGS);

		}
		catch (Exception e)
		{
			Log.e(TAG, "Problem generating hash", e);
		}
		return "";
	}

	/**
	 * Encrypts given data
	 */
	public String encrypt(String data)
	{
		if (isValidString(data))
		{
			try
			{
				return AesCbcWithIntegrity.encrypt(data, mSecretKeys).toString();
			}
			catch (Exception e)
			{
				Log.e(TAG, "Could not encrypt this string", e);
			}
		}
		return "";
	}

	/**
	 * Decrypts encryptedData
	 */
	public String decrypt(String encryptedData)
	{
		return decrypt(encryptedData, mSecretKeys);
	}

	/**
	 * Decrypts encryptedData
	 */
	public static String decrypt(String encryptedData, AesCbcWithIntegrity.SecretKeys secretKeys)
	{
		if (isValidString(encryptedData))
		{
			try
			{
				AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(encryptedData);
				return AesCbcWithIntegrity.decryptString(cipherTextIvMac, secretKeys);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Could not decrypt this string", e);
			}
		}
		return "";
	}

	/**
	 * Gets the hardware serial number of this device.
	 *
	 * @return serial number or Settings.Secure.ANDROID_ID if not available.
	 */
	public static String getDeviceID(Context context)
	{
		return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	private byte[] getSalt()
	{
		String id = getDeviceID(mContext);
		if (!isValidString(id))
		{
			id = "ROBOLECTRICYOUAREBAD";
		}
		byte[] salt = Arrays.copyOf(getValidStringBytes(id), 8);
		return salt;
	}

	public static boolean isValidString(String string)
	{
		return !TextUtils.isEmpty(string);
	}

	public static byte[] getValidStringBytes(String string)
	{
		if (isValidString(string))
		{
			try
			{
				return string.getBytes("UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				return string.getBytes();
			}
		}
		return new byte[0];
	}
}
