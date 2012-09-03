package com.anddev.images.processors;

import android.graphics.Bitmap;

import com.anddev.images.ImageToLoad;

/**
 * An interface specifying a way to process an image.
 * 
 * @author Mantas Varnagiris
 */
public interface ImageProcessor
{
	/**
	 * Modifies given bitmap. Should recycle given bitmap here.
	 * 
	 * @param bitmap
	 *            The Bitmap to process.
	 * @return A Bitmap that has been modified.
	 */
	public Bitmap processImage(Bitmap bitmap);

	/**
	 * Generates unique ID for this class. Used to generate {@link ImageToLoad#memoryName}.
	 * <p>
	 * Because of this, we can identify images that has been processed with two different {@link ImageProcessor} as two different images.
	 * </p>
	 * 
	 * @return Unique class ID.
	 */
	public String getUniqueId();
}