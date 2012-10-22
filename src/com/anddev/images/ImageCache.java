package com.anddev.images;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.anddev.AndDevSettings;
import com.anddev.BuildConfig;
import com.anddev.images.info.FileBitmapInfo;
import com.anddev.utils.APIUtils;

public class ImageCache
{
	private static final String				PRE_FROYO_EXTERNAL_PATH		= File.separator + "Android" + File.separator + "data" + File.separator
																						+ ImageCache.class.getPackage().getName() + File.separator + "cache";

	protected static final CompressFormat	DEFAULT_COMPRESS_FORMAT		= CompressFormat.PNG;
	protected static final int				DEFAULT_COMPRESS_QUALITY	= 90;

	private static ImageCache				instance					= null;

	private Context							context;
	private LruCache<String, Bitmap>		memoryCache;
	private File							fileCacheDir;

	public static ImageCache getInstance(Context context)
	{
		if (instance == null)
			instance = new ImageCache(context);
		return instance;
	}

	private ImageCache(Context context)
	{
		this.context = context.getApplicationContext();
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 6;
		memoryCache = new LruCache<String, Bitmap>(cacheSize)
		{
			@TargetApi(12)
			@Override
			protected int sizeOf(String key, Bitmap bitmap)
			{
				if (APIUtils.SUPPORTS_HONEYCOMB_MR1)
				{
					return bitmap.getByteCount();
				}
				// Pre HC-MR1
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};

		final String state = Environment.getExternalStorageState();
		final File externalCacheDir = APIUtils.SUPPORTS_FROYO ? context.getExternalCacheDir() : new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + PRE_FROYO_EXTERNAL_PATH);
		final File internalCacheDir = context.getCacheDir();
		if (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) && checkWriteExternalPermission())
			fileCacheDir = externalCacheDir;
		else
			fileCacheDir = internalCacheDir;

		// We have to make sure cache dir exists
		if (!fileCacheDir.exists())
			fileCacheDir.mkdirs();

		// Try to clean other cache directory. We need that because external cache directory can be removed and we might need to switch between them.
		if (fileCacheDir.equals(internalCacheDir))
			cleanFilePath(externalCacheDir);
		else
			cleanFilePath(internalCacheDir);
	}

	// Public methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Tries to get image from memory cache.
	 * 
	 * @param memoryName
	 *            Key for bitmap in memory cache.
	 * @return Bitmap from memory cache or {@code null}.
	 */
	public Bitmap getFromMemory(final String memoryName)
	{
		return memoryCache.get(memoryName);
	}

	public Bitmap getFromFile(ImageToLoad imageToLoad)
	{
		final File bitmapFile = new File(fileCacheDir, imageToLoad.fileName);
		return new FileBitmapInfo(bitmapFile.getAbsolutePath()).getBitmapFetcher(context).getBitmap(imageToLoad);
	}

	public File getFile(final String fileName)
	{
		return new File(fileCacheDir, fileName);
	}

	public boolean putToMemory(final String memoryName, final Bitmap bitmap)
	{
		if (memoryCache.get(memoryName) == null)
		{
			memoryCache.put(memoryName, bitmap);
			return true;
		}

		return false;
	}

	public boolean putToFile(final String fileName, final Bitmap bitmap)
	{
		File bitmapFile = new File(fileCacheDir, fileName);
		if (!bitmapFile.exists())
		{
			try
			{
				FileOutputStream fos = new FileOutputStream(bitmapFile, false);
				bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, fos);
				return true;
			}
			catch (FileNotFoundException e)
			{
				if (BuildConfig.DEBUG && AndDevSettings.Logging.LOG_IMAGE_LOADER)
					Log.w(ImageLoader.TAG, "Failed saving to file cache. " + fileName);
				e.printStackTrace();
			}
		}
		return false;
	}

	public File getFileCacheDir()
	{
		return fileCacheDir;
	}

	// Private methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	private void cleanFilePath(File filePathToClean)
	{
		if (filePathToClean != null)
		{
			File[] files = filePathToClean.listFiles();
			if (files != null)
				for (File file : files)
					file.delete();
		}
	}

	private boolean checkWriteExternalPermission()
	{
		String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
		int res = context.checkCallingOrSelfPermission(permission);
		return res == PackageManager.PERMISSION_GRANTED;
	}
}