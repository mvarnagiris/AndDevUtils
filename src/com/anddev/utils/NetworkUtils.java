package com.anddev.utils;

import android.Manifest.permission;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Various network utilities.
 * 
 * @author Mantas Varnagiris
 */
public class NetworkUtils
{
	/**
	 * Checks if device has any network connectivity at the moment.
	 * <p>
	 * Requires {@link permission#ACCESS_NETWORK_STATE}.
	 * </p>
	 * 
	 * @param context
	 *            Context
	 * @return {@code true} if device has network connection; {@code false} otherwise.
	 */
	public static boolean hasNewtorkConnection(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	/**
	 * Workaround for bug pre-Froyo, see here for more info: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary()
	{
		// HTTP connection reuse which was buggy pre-froyo
		if (!APIUtils.SUPPORTS_FROYO)
		{
			System.setProperty("http.keepAlive", "false");
		}
	}
}