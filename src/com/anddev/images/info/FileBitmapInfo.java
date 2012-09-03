package com.anddev.images.info;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.anddev.BuildConfig;
import com.anddev.images.ImageLoader;
import com.anddev.images.ImageToLoad;
import com.anddev.utils.ImageUtils;

/**
 * Loads bitmap from file.
 * 
 * @author Mantas Varnagiris
 */
public class FileBitmapInfo extends BitmapInfo
{
	public final String	filePath;

	public FileBitmapInfo(String filePath)
	{
		this.filePath = filePath;
	}

	@Override
	public String getUniqueName()
	{
		return filePath;
	}

	@Override
	public BitmapFetcher getBitmapFetcher(Context context)
	{
		return FileBitmapFetcher.getInstance(context);
	}

	// FileBitmapFetcher
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static class FileBitmapFetcher extends BitmapFetcher
	{
		private static FileBitmapFetcher	instance	= null;

		public static FileBitmapFetcher getInstance(Context context)
		{
			if (instance == null)
				instance = new FileBitmapFetcher(context);
			return instance;
		}

		protected FileBitmapFetcher(Context context)
		{
			super(context);
		}

		@Override
		public Bitmap getBitmap(ImageToLoad imageToLoad)
		{
			Bitmap bitmap = null;
			File bitmapFile = getBitmapFile(imageToLoad);

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
					BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
					options.inSampleSize = ImageUtils.calculateInSampleSize(options.outWidth, options.outHeight, width, height);
					options.inJustDecodeBounds = false;
				}

				if (BuildConfig.DEBUG)
					Log.d(ImageLoader.TAG, "inSampleSize = " + options.inSampleSize + ". " + imageToLoad.bitmapInfo.getUniqueName());

				// Load bitmap from file
				bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);
			}
			catch (OutOfMemoryError e)
			{
				if (BuildConfig.DEBUG)
					Log.e(ImageLoader.TAG, "OutOfMemoryError. " + imageToLoad.bitmapInfo.getUniqueName());
				e.printStackTrace();
			}
			catch (Exception e)
			{
				if (BuildConfig.DEBUG)
					Log.w(ImageLoader.TAG, "Error loading bitmap. " + imageToLoad.bitmapInfo.getUniqueName());
				e.printStackTrace();
			}

			return bitmap;
		}

		// Protected methods
		// ------------------------------------------------------------------------------------------------------------------------------------

		protected File getBitmapFile(ImageToLoad imageToLoad)
		{
			return new File(imageToLoad.bitmapInfo.getUniqueName());
		}
	}
}