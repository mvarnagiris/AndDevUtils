package com.anddev.images;

import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.anddev.BuildConfig;
import com.anddev.images.info.BitmapInfo;
import com.anddev.images.processors.ImageProcessor;

public class ImageLoader
{
	public static final String	TAG			= "ImageLoader";

	private static ImageLoader	instance	= null;

	private Context				context;
	private ImageCache			imageCache;

	public static ImageLoader getInstance(Context context)
	{
		if (instance == null)
			instance = new ImageLoader(context);
		return instance;
	}

	private ImageLoader(Context context)
	{
		this.context = context.getApplicationContext();
		imageCache = ImageCache.getInstance(context.getApplicationContext());
	}

	// Public methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	public void loadImage(final ImageView imageView, final BitmapInfo bitmapInfo, ImageParams params)
	{
		if (imageView == null || bitmapInfo == null)
			return;

		if (params == null)
			params = new ImageParams(ImageParams.NO_SIZE, ImageParams.NO_SIZE, null, null);

		if (TextUtils.isEmpty(bitmapInfo.getUniqueName()))
		{
			// TODO Log
			imageView.setImageBitmap(params.placeholder);
			return;
		}

		final ImageToLoad imageToLoad = new ImageToLoad(imageView, bitmapInfo, params);

		// Try to get bitmap from cache
		if (params.useMemoryCache)
		{
			Bitmap bitmap = imageCache.getFromMemory(imageToLoad.memoryName);
			if (bitmap != null)
			{
				if (BuildConfig.DEBUG)
					Log.d(ImageLoader.TAG, "Memory cache. " + imageToLoad.bitmapInfo.getUniqueName());
				imageView.setImageBitmap(bitmap);
				if (params.listener != null)
					params.listener.onImageLoaderFinished(imageToLoad, bitmap);
				return;
			}
		}

		// If bitmap was not found in cache and same work is not already running - load it
		if (imageToLoad.cancelPotentialWork())
		{
			try
			{
				final GetBitmapTask getBitmapTask = new GetBitmapTask(imageToLoad);
				final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), params.placeholder, getBitmapTask);
				imageView.setImageDrawable(asyncDrawable);
				getBitmapTask.execute();
			}
			catch (RejectedExecutionException e)
			{
				e.printStackTrace();
			}
		}
	}

	// Protected methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	protected void setBitmap(ImageView imageView, Bitmap bitmap, Bitmap placeholder)
	{
		final BitmapDrawable drawable = new BitmapDrawable(imageView.getResources(), bitmap);
		imageView.setImageDrawable(drawable);
	}

	// AsyncTask
	// ------------------------------------------------------------------------------------------------------------------------------------

	public class GetBitmapTask extends AsyncTask<Void, Void, Bitmap>
	{
		public final ImageToLoad	imageToLoad;

		public GetBitmapTask(ImageToLoad imageToLoad)
		{
			this.imageToLoad = imageToLoad;
		}

		@Override
		protected Bitmap doInBackground(Void... params)
		{
			Bitmap bitmap = null;

			// Try to get bitmap from file cache
			if (!isCancelled() && imageToLoad.params.useFileCache)
			{
				bitmap = imageCache.getFromFile(imageToLoad);
				if (BuildConfig.DEBUG && bitmap != null)
					Log.d(ImageLoader.TAG, "File cache. " + imageToLoad.bitmapInfo.getUniqueName());
			}

			// Load bitmap and put it to file cache
			if (bitmap == null && !isCancelled())
			{
				// Get bitmap
				bitmap = imageToLoad.loadBitmap(context);

				// Save to file cache
				if (bitmap != null && imageToLoad.params.useFileCache && imageCache.putToFile(imageToLoad.fileName, bitmap) && BuildConfig.DEBUG)
					Log.d(ImageLoader.TAG, "Saving to file cache. " + imageToLoad.bitmapInfo.getUniqueName());
			}

			// Process image
			if (bitmap != null && !isCancelled())
			{
				final ImageProcessor imageProcessor = imageToLoad.params.imageProcessor;
				if (imageProcessor != null)
					bitmap = imageProcessor.processImage(bitmap);
			}

			// Put to memory
			if (bitmap != null && imageToLoad.params.useMemoryCache)
			{
				synchronized (imageCache)
				{
					if (imageCache.putToMemory(imageToLoad.memoryName, bitmap) && BuildConfig.DEBUG)
						Log.d(ImageLoader.TAG, "Saving to memory cache. " + imageToLoad.bitmapInfo.getUniqueName());
				}

			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			if (isCancelled())
			{
				bitmap = null;
				return;
			}

			final ImageView imageView = imageToLoad.getImageView();
			if (imageView != null && bitmap != null && imageToLoad.getGetBitmapTask() == this)
			{
				setBitmap(imageView, bitmap, imageToLoad.params.placeholder);
			}

			if (imageToLoad.params.listener != null)
				imageToLoad.params.listener.onImageLoaderFinished(imageToLoad, bitmap);
		}
	}

	// AsyncDrawable
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static class AsyncDrawable extends BitmapDrawable
	{
		private final WeakReference<GetBitmapTask>	getBitmapTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, GetBitmapTask getBitmapTask)
		{
			super(res, bitmap);
			getBitmapTaskReference = new WeakReference<GetBitmapTask>(getBitmapTask);
		}

		public GetBitmapTask getGetBitmapTask()
		{
			return getBitmapTaskReference.get();
		}
	}

	// Interfaces
	// ------------------------------------------------------------------------------------------------------------------------------------

	public static interface ImageLoaderListener
	{
		public void onImageLoaderFinished(ImageToLoad imageToLoad, Bitmap bitmap);
	}
}