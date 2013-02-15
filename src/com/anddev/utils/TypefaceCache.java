package com.anddev.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

public class TypefaceCache
{
	public enum TypefaceCode
	{
		ROBOTO_REGULAR, ROBOTO_LIGHT, ROBOTO_THIN, ROBOTO_CONDENSED
	};

	private static final Map<String, Typeface>	typefaceMap	= new HashMap<String, Typeface>();

	// Public static methods
	// --------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Tries to get typeface from cache or create a new one if not found.
	 * 
	 * @param context
	 *            Context for creating typeface
	 * @param typefaceCode
	 *            Typeface code.
	 * @return Typeface or {@code null}.
	 */
	public static Typeface getTypeface(Context context, TypefaceCode typefaceCode)
	{
		final String typefacePath = getTypefacePath(typefaceCode);
		if (!TextUtils.isEmpty(typefacePath))
		{
			Typeface typeface = typefaceMap.get(typefacePath);
			if (typeface == null)
			{
				typeface = Typeface.createFromAsset(context.getAssets(), typefacePath);
				typefaceMap.put(typefacePath, typeface);
			}
			return typeface;
		}

		return null;
	}

	// Private static methods
	// --------------------------------------------------------------------------------------------------------------------------------

	private static String getTypefacePath(TypefaceCode typefaceCode)
	{
		switch (typefaceCode)
		{
			case ROBOTO_REGULAR:
				return "fonts/Roboto-Regular.ttf";

			case ROBOTO_LIGHT:
				return "fonts/Roboto-Light.ttf";

			case ROBOTO_THIN:
				return "fonts/Roboto-Thin.ttf";

			case ROBOTO_CONDENSED:
				return "fonts/Roboto-Condensed.ttf";
		}

		return null;
	}
}