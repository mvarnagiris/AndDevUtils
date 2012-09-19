package com.anddev.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtils
{
	public static boolean checkPermission(Context context, String permission)
	{
		int res = context.checkCallingOrSelfPermission(permission);
		return res == PackageManager.PERMISSION_GRANTED;
	}
}