package com.anddev.images.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.anddev.AndDevSettings;
import com.anddev.BuildConfig;
import com.anddev.images.ImageLoader;
import com.anddev.images.ImageToLoad;
import com.anddev.utils.ImageUtils;

public class ResourceBitmapInfo extends BitmapInfo
{
	public int	resId;

	public ResourceBitmapInfo(int resId)
	{
		this.resId = resId;
	}

	@Override
	public String getUniqueName()
	{
		return "ResourseImage_" + String.valueOf(resId);
	}

	@Override
	public BitmapFetcher getBitmapFetcher(Context context)
	{
		return ResourceBitmapFetcher.getInstance(context);
	}

	// ResourceBitmapFetcher
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static class ResourceBitmapFetcher extends BitmapFetcher
	{
		private static ResourceBitmapFetcher	instance	= null;

		public static ResourceBitmapFetcher getInstance(Context context)
		{
			if (instance == null)
				instance = new ResourceBitmapFetcher(context);
			return instance;
		}

		protected ResourceBitmapFetcher(Context context)
		{
			super(context);
		}

		@Override
		public Bitmap getBitmap(ImageToLoad imageToLoad)
		{
			Bitmap bitmap = null;
			try
			{
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPurgeable = true;
				options.inInputShareable = true;

				// If image has width and height, set inSampleSize to scale it
				final int width = imageToLoad.params.width;
				final int height = imageToLoad.params.height;
				if (width > 0 && height > 0)
				{
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeResource(context.getResources(), ((ResourceBitmapInfo) imageToLoad.bitmapInfo).resId, options);
					options.inSampleSize = ImageUtils.calculateInSampleSize(options.outWidth, options.outHeight, width, height);
					options.inJustDecodeBounds = false;
				}

				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.d(ImageLoader.TAG, "inSampleSize = " + options.inSampleSize + ". " + imageToLoad.bitmapInfo.getUniqueName());

				// Load bitmap from resource
				bitmap = BitmapFactory.decodeResource(context.getResources(), ((ResourceBitmapInfo) imageToLoad.bitmapInfo).resId, options);
			}
			catch (OutOfMemoryError e)
			{
				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.e(ImageLoader.TAG, "OutOfMemoryError. " + imageToLoad.bitmapInfo.getUniqueName());
				e.printStackTrace();
			}
			catch (Exception e)
			{
				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.w(ImageLoader.TAG, "Error loading bitmap. " + imageToLoad.bitmapInfo.getUniqueName());
				e.printStackTrace();
			}

			return bitmap;
		}
	}
}