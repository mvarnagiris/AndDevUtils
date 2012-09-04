package com.anddev.utils;

public class APIUtils
{
	public static final boolean	SUPPORTS_ECLAIR			= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR;
	public static final boolean	SUPPORTS_FROYO			= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;
	public static final boolean	SUPPORTS_GINGERBREAD	= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
	public static final boolean	SUPPORTS_HONEYCOMB		= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
	public static final boolean	SUPPORTS_HONEYCOMB_MR1	= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1;
	public static final boolean	SUPPORTS_HONEYCOMB_MR2	= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2;
	public static final boolean	SUPPORTS_ICS			= android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
}