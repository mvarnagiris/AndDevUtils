package com.anddev.images;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.anddev.images.ImageLoader.ImageLoaderListener;
import com.anddev.images.processors.ChainImageProcessor;
import com.anddev.images.processors.ImageProcessor;
import com.anddev.images.processors.ScaleImageProcessor;
import com.anddev.images.processors.ScaleImageProcessor.ScaleType;

public class ImageParams
{
	public static final int		NO_SIZE	= -1;

	/** Width of {@link ImageView}. Default {@link Params#NO_SIZE}. */
	public final int			width;

	/** Height of {@link ImageView}. Default {@link Params#NO_SIZE}. */
	public final int			height;

	/** Image will be modified by given processor. Default is based on passed in {@link ScaleType}. */
	public final ImageProcessor	imageProcessor;

	/** If {@code true}, bitmap will be set automatically. If {@code false}, use {@link ImageLoaderListener} to set images yourself. Default {@code true}. */
	public boolean				setImagesAutomatically;

	/** If {@code true}, bitmap will be stored in file cache. Default {@code true}. */
	public boolean				useFileCache;

	/** If {@code true}, bitmap will be stored in memory cache. Default {@code true}. */
	public boolean				useMemoryCache;

	/** Placeholder to set on {@link ImageView} while image is loading. Default {@code null}. */
	public Bitmap				placeholder;

	/** Listener that gets callbacks when image loads. Default {@code null}. */
	public ImageLoaderListener	listener;

	public ImageParams(final int width, final int height, final ScaleType scaleType, ImageProcessor imageProcessor)
	{
		this.width = width;
		this.height = height;
		ImageProcessor tempImageProcessor = null;
		if (scaleType != null && scaleType != ScaleType.NONE && width > 0 && height > 0)
			tempImageProcessor = new ScaleImageProcessor(width, height, scaleType);
		if (tempImageProcessor != null && imageProcessor != null)
			this.imageProcessor = new ChainImageProcessor(tempImageProcessor, imageProcessor);
		else if (tempImageProcessor != null)
			this.imageProcessor = tempImageProcessor;
		else
			this.imageProcessor = imageProcessor;
		setImagesAutomatically = true;
		useFileCache = true;
		useMemoryCache = true;
		imageProcessor = null;
		placeholder = null;
		listener = null;
	}
}
