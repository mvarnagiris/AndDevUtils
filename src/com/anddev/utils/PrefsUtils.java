package com.anddev.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * {@link SharedPreferences} utilities.
 * 
 * @author Mantas Varnagiris
 */
public class PrefsUtils
{
	public static final SharedPreferences getPrefs(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static class WorkServicePrefs
	{
		private static final String	PREFIX	= "work_service_";

		/** Stores last successful sync date. */
		public static final String getLastSuccessfulWorkTimePrefName(String prefix, int requestType, String suffix)
		{
			return PREFIX + prefix + "_" + requestType + (suffix != null ? suffix : "");
		}
	}
}