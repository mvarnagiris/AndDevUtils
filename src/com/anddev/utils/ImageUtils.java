package com.anddev.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

/**
 * Various utilities for images.
 * 
 * @author Mantas Varnagiris
 */
public class ImageUtils
{
	/**
	 * Calculates optimal inSampleSize for bitmap.
	 * 
	 * @param bitmapWidth
	 *            Bitmap width.
	 * @param bitmapHeight
	 *            Bitmap height.
	 * @param reqWidth
	 *            Required width.
	 * @param reqHeight
	 *            Required height.
	 * @return Calculated best inSampleSize.
	 */
	public static int calculateInSampleSize(final int bitmapWidth, final int bitmapHeight, final int reqWidth, final int reqHeight)
	{
		// Raw height and width of image
		int inSampleSize = 1;

		if (bitmapHeight > reqHeight || bitmapWidth > reqWidth)
		{
			if (bitmapWidth > bitmapHeight)
				inSampleSize = Math.round((float) bitmapHeight / (float) reqHeight);
			else
				inSampleSize = Math.round((float) bitmapWidth / (float) reqWidth);
		}
		return inSampleSize;
	}

	/**
	 * Notifies media scanner about the image, so it will appear in gallery.
	 * 
	 * @param context
	 * @param bitmapFile
	 */
	public static void notifyMediaScanner(final Context context, final String bitmapFilePath)
	{
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(new File(bitmapFilePath));
		mediaScanIntent.setData(contentUri);
		context.sendBroadcast(mediaScanIntent);
	}

	/**
	 * Saves bitmap to file path.
	 * 
	 * @param fullPath
	 *            Full file path including image name.
	 * @param bitmap
	 *            Bitmap to save.
	 * @throws FileNotFoundException
	 */
	public static void saveBitmapToFile(final String fullPath, final Bitmap bitmap) throws FileNotFoundException
	{
		File bitmapFile = new File(fullPath);
		FileOutputStream fos = new FileOutputStream(bitmapFile, false);
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
	}

	/**
	 * @param albumName
	 *            Name of the album
	 * @return File for album in images directory.
	 */
	public static File getPublicImagesDirectory(final String albumName)
	{
		File albumF = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			albumF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
		else
			albumF = new File(Environment.getExternalStorageDirectory() + "/dcim/" + albumName);

		if (albumF != null && !albumF.mkdirs() && !albumF.exists())
			albumF = null;

		return albumF;
	}
}