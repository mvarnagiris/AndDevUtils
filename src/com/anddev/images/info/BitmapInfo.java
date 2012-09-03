package com.anddev.images.info;

import android.content.Context;
import android.graphics.Bitmap;

import com.anddev.images.ImageToLoad;

/**
 * @author Mantas Varnagiris
 */
public abstract class BitmapInfo
{
	public abstract String getUniqueName();

	public abstract BitmapFetcher getBitmapFetcher(Context context);

	// BitmapFetcher
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static abstract class BitmapFetcher
	{
		protected Context	context;

		public BitmapFetcher(Context context)
		{
			this.context = context.getApplicationContext();
		}

		// Abstract methods
		// ------------------------------------------------------------------------------------------------------------------------------------

		public abstract Bitmap getBitmap(ImageToLoad imageToLoad);
	}
}