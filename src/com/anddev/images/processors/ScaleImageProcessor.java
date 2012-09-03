package com.anddev.images.processors;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Scales Bitmaps according to a given width and height. The scaling method may be one of the {@link ScaleType} values.
 * 
 * @author Mantas Varnagiris
 */
public class ScaleImageProcessor implements ImageProcessor
{
	public enum ScaleType
	{
		NONE, CENTER_CROP, CENTER_INSIDE, CENTER, FIT_XY, SCALE_AND_CROP_TO_FILL
	}

	private int				targetWidth;
	private int				targetHeight;
	private ScaleType		scaleType;
	private final Matrix	matrix		= new Matrix();

	private final RectF		tempSrcRect	= new RectF();
	private final RectF		tempDstRect	= new RectF();

	/**
	 * Create a new ScaleImageProcessor.
	 * 
	 * @param width
	 *            The width of the final surrounding box
	 * @param height
	 *            The height of the final surrounding box
	 * @param scaleType
	 *            The ScaleType method used to scale the original Bitmap
	 */
	public ScaleImageProcessor(int width, int height, ScaleType scaleType)
	{
		this.targetWidth = width;
		this.targetHeight = height;
		this.scaleType = scaleType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bitmap processImage(Bitmap bitmap)
	{
		if (bitmap == null)
			return null;

		matrix.reset();

		final int bWidth = bitmap.getWidth();
		final int bHeight = bitmap.getHeight();

		switch (scaleType)
		{

			case CENTER_CROP:
			{
				// Center and scale the bitmap so that it entirely fills the given space. The bitmap ratio remains unchanged
				float scale;
				float dx = 0, dy = 0;

				if (bWidth * targetHeight > targetWidth * bHeight)
				{
					scale = (float) targetHeight / (float) bHeight;
					dx = (targetWidth - bWidth * scale) * 0.5f;
				}
				else
				{
					scale = (float) targetWidth / (float) bWidth;
					dy = (targetHeight - bHeight * scale) * 0.5f;
				}

				matrix.setScale(scale, scale);
				matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
				break;
			}

			case CENTER:
			{
				// Center bitmap without scaling
				final int dx = (int) ((targetWidth - bWidth) * 0.5f + 0.5f);
				final int dy = (int) ((targetHeight - bHeight) * 0.5f + 0.5f);
				matrix.setTranslate(dx, dy);
				break;
			}

			case CENTER_INSIDE:
			{
				// Center and scale the bitmap so that it entirely fits into the given space.
				float scale;
				float dx;
				float dy;

				if (bWidth <= targetWidth && bHeight <= targetHeight)
				{
					scale = 1.0f;
				}
				else
				{
					scale = Math.min((float) targetWidth / (float) bWidth, (float) targetHeight / (float) bHeight);
				}

				dx = (int) ((targetWidth - bWidth * scale) * 0.5f + 0.5f);
				dy = (int) ((targetHeight - bHeight * scale) * 0.5f + 0.5f);

				matrix.setScale(scale, scale);
				matrix.postTranslate(dx, dy);
				break;
			}

			case SCALE_AND_CROP_TO_FILL:
			{
				// Center scale and crop the bitmap so that it entirely fits into the given space.
				float scale;
				float dx;
				float dy;

				if (bWidth <= targetWidth && bHeight <= targetHeight)
				{
					scale = Math.max((float) targetWidth / (float) bWidth, (float) targetHeight / (float) bHeight);
				}
				else
				{
					scale = Math.max((float) targetWidth / (float) bWidth, (float) targetHeight / (float) bHeight);
				}

				dx = (int) ((targetWidth - bWidth * scale) * 0.5f + 0.5f);
				dy = (int) ((targetHeight - bHeight * scale) * 0.5f + 0.5f);

				matrix.setScale(scale, scale);
				matrix.postTranslate(dx, dy);
				break;
			}

			case FIT_XY:
			default:
				// Entirely fills the space without respecting bitmap's ratio.
				tempSrcRect.set(0, 0, bWidth, bHeight);
				tempDstRect.set(0, 0, targetWidth, targetHeight);

				matrix.setRectToRect(tempSrcRect, tempDstRect, Matrix.ScaleToFit.FILL);
				break;
		}

		try
		{

			Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(result);
			canvas.drawBitmap(bitmap, matrix, null);

			bitmap.recycle();
			bitmap = null;

			return result;
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUniqueId()
	{
		return ScaleImageProcessor.class.getName() + targetWidth + "x" + targetHeight;
	}
}