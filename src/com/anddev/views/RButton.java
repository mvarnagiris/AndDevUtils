package com.anddev.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.anddev.R;
import com.anddev.utils.TypefaceCache;
import com.anddev.utils.TypefaceCache.TypefaceCode;

public class RButton extends Button
{
	public RButton(Context context)
	{
		super(context);
		init(context, null);
	}

	public RButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public RButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	public void setTypeface(TypefaceCode typefaceCode)
	{
		if (isInEditMode())
			return;

		final Typeface typeface = TypefaceCache.getTypeface(getContext(), typefaceCode);
		if (typeface != null)
			setTypeface(typeface);
	}

	// Private methods
	// --------------------------------------------------------------------------------------------------------------------------------

	private void init(Context context, AttributeSet attrs)
	{
		if (attrs != null)
		{
			// Get attributes
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RButton, 0, 0);
			try
			{
				final int typefacePosition = a.getInteger(R.styleable.RButton_roboto_typeface, -1);
				if (typefacePosition >= 0)
					setTypeface(TypefaceCode.values()[typefacePosition]);
			}
			finally
			{
				a.recycle();
			}
		}
	}
}