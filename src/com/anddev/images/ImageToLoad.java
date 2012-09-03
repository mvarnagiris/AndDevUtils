package com.anddev.images;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.anddev.images.ImageLoader.AsyncDrawable;
import com.anddev.images.ImageLoader.GetBitmapTask;
import com.anddev.images.info.BitmapInfo;
import com.anddev.images.info.BitmapInfo.BitmapFetcher;
import com.anddev.utils.StringUtils;

/**
 * Carries information about bitmap that needs loading.
 * 
 * @author Mantas Varnagiris
 */
public class ImageToLoad
{
	public final BitmapInfo					bitmapInfo;
	public final ImageParams				params;
	public final String						fileName;
	public final String						memoryName;

	private final WeakReference<ImageView>	imageViewReference;

	public ImageToLoad(ImageView imageView, BitmapInfo bitmapInfo, ImageParams params)
	{
		this.bitmapInfo = bitmapInfo;
		this.params = params;

		this.fileName = StringUtils.md5(bitmapInfo.getUniqueName());
		this.memoryName = StringUtils.md5(bitmapInfo.getUniqueName() + "_" + params.width + "x" + params.height
						+ (params.imageProcessor != null ? "_" + params.imageProcessor.getUniqueId() : ""));

		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	// Public methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * @return {@link ImageView} reference or {@code null}.
	 */
	public ImageView getImageView()
	{
		if (imageViewReference != null)
			return imageViewReference.get();

		return null;
	}

	/**
	 * Uses {@link BitmapInfo} to get {@link BitmapFetcher} and load bitmap.
	 * 
	 * @param context
	 *            Context for bitmap fetcher.
	 * @return Loaded bitmap or {@code null}.
	 */
	public Bitmap loadBitmap(Context context)
	{
		return bitmapInfo.getBitmapFetcher(context).getBitmap(this);
	}

	/**
	 * Tries to cancel queued work for this bitmap.
	 * 
	 * @return {@code true} if canceled; {@code false} otherwise.
	 */
	public boolean cancelPotentialWork()
	{
		GetBitmapTask getBitmapTask = getGetBitmapTask();

		if (getBitmapTask != null)
		{
			final String tempMemoryName = getBitmapTask.imageToLoad.memoryName;
			if (!tempMemoryName.equalsIgnoreCase(memoryName))
				getBitmapTask.cancel(true);
			else
				return false;
		}

		return true;
	}

	/**
	 * @return Bitmap load task or {@code null};
	 */
	public GetBitmapTask getGetBitmapTask()
	{
		ImageView imageView = getImageView();
		if (imageView != null)
		{
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable)
			{
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getGetBitmapTask();
			}
		}

		return null;
	}
}