package com.anddev.images.info;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.anddev.AndDevSettings;
import com.anddev.BuildConfig;
import com.anddev.images.ImageLoader;
import com.anddev.images.ImageToLoad;
import com.anddev.images.info.FileBitmapInfo.FileBitmapFetcher;
import com.anddev.utils.NetworkUtils;

public class URLBitmapInfo extends BitmapInfo
{
	public String	imageURL;

	public URLBitmapInfo(String imageURL)
	{
		this.imageURL = imageURL;
	}

	@Override
	public String getUniqueName()
	{
		return imageURL;
	}

	@Override
	public BitmapFetcher getBitmapFetcher(Context context)
	{
		return URLBitmapFetcher.getInstance(context);
	}

	// URLBitmapFetcher
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static class URLBitmapFetcher extends FileBitmapFetcher
	{
		protected static final int		IO_BUFFER_SIZE	= 8 * 1024;

		private static URLBitmapFetcher	instance		= null;

		public static URLBitmapFetcher getInstance(Context context)
		{
			if (instance == null)
				instance = new URLBitmapFetcher(context);
			return instance;
		}

		protected URLBitmapFetcher(Context context)
		{
			super(context);
		}

		@Override
		public Bitmap getBitmap(ImageToLoad imageToLoad)
		{
			final Bitmap bitmap = super.getBitmap(imageToLoad);

			final File tempFile = new File(context.getCacheDir(), imageToLoad.fileName);
			if (tempFile.exists())
				tempFile.delete();

			return bitmap;
		}

		@Override
		protected File getBitmapFile(ImageToLoad imageToLoad)
		{
			// Create file to store bitmap
			final File tempFile = new File(context.getCacheDir(), imageToLoad.fileName);

			// Setup connection
			NetworkUtils.disableConnectionReuseIfNecessary();
			HttpURLConnection urlConnection = null;
			BufferedOutputStream out = null;
			URL url = null;

			try
			{
				// Download bitmap to temporary file
				url = new URL(((URLBitmapInfo) imageToLoad.bitmapInfo).imageURL);

				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.d(ImageLoader.TAG, "Downloading bitmap to file. " + url);

				urlConnection = (HttpURLConnection) url.openConnection();
				final InputStream in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
				out = new BufferedOutputStream(new FileOutputStream(tempFile), IO_BUFFER_SIZE);

				int b;
				while ((b = in.read()) != -1)
				{
					out.write(b);
				}

				in.close();

				return tempFile;
			}
			catch (final MalformedURLException e)
			{
				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.w(ImageLoader.TAG, "Bad url. " + url);
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.w(ImageLoader.TAG, "Failed downloading bitmap to file. " + url);
				e.printStackTrace();
			}
			finally
			{
				// Close connection
				if (urlConnection != null)
					urlConnection.disconnect();

				// Close stream
				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			return null;
		}
	}
}