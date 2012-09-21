package com.anddev.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.anddev.images.ImageLoader;
import com.anddev.images.ImageParams;
import com.anddev.images.info.BitmapInfo;
import com.anddev.images.processors.ScaleImageProcessor;

public class LoaderImageView extends ImageView
{
	private final ImageLoader	imageLoader		= ImageLoader.getInstance(getContext());
	private ImageParams			params			= null;
	private ImageParams			paramsToCopy	= null;
	private BitmapInfo			bitmapInfo		= null;

	public LoaderImageView(Context context)
	{
		super(context);
	}

	public LoaderImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public LoaderImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		if (w > 0 && h > 0)
		{
			boolean needLoadImages = params == null && bitmapInfo != null;
			params = new ImageParams(w, h, paramsToCopy != null ? paramsToCopy.scaleType : ScaleImageProcessor.ScaleType.SCALE_AND_CROP_TO_FILL,
							paramsToCopy != null ? paramsToCopy.imageProcessor : null);
			copyParams();
			if (needLoadImages)
			{
				if (params != null && bitmapInfo != null)
					imageLoader.loadImage(this, bitmapInfo, params);
			}
		}
		else
		{
			params = null;
		}
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Loads image using view size.
	 * 
	 * @param bitmapInfo
	 *            Image to load.
	 */
	public void loadImage(BitmapInfo bitmapInfo)
	{
		this.bitmapInfo = bitmapInfo;
		if (params != null && bitmapInfo != null)
			imageLoader.loadImage(this, bitmapInfo, params);
	}

	public void setParamsToCopy(ImageParams paramsToCopy)
	{
		this.paramsToCopy = paramsToCopy;
	}

	// Private methods
	// --------------------------------------------------------------------------------------------------------------------------------

	private void copyParams()
	{
		if (paramsToCopy != null)
		{
			params.useFileCache = paramsToCopy.useFileCache;
			params.useMemoryCache = paramsToCopy.useMemoryCache;
			params.setImagesAutomatically = paramsToCopy.setImagesAutomatically;
			params.placeholder = paramsToCopy.placeholder;
			params.listener = paramsToCopy.listener;
		}
	}
}