package com.anddev.images.processors;

import android.graphics.Bitmap;

/**
 * Allows multiple image processors to be chained.
 * 
 * @author Mantas Varnagiris
 */
public class ChainImageProcessor implements ImageProcessor
{
	ImageProcessor[]	processors;

	public ChainImageProcessor(ImageProcessor... processors)
	{
		this.processors = processors;
	}

	@Override
	public Bitmap processImage(Bitmap bitmap)
	{
		for (ImageProcessor processor : processors)
			bitmap = processor.processImage(bitmap);
		return bitmap;
	}

	@Override
	public String getUniqueId()
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < processors.length; i++)
			builder.append("." + processors[i].getUniqueId());
		return ChainImageProcessor.class.getName() + builder.toString();
	}
}