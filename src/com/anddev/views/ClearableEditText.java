package com.anddev.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

import com.anddev.R;

/**
 * EditText that allows to clear text with one click. When user enters text, clear button appears on the right.
 * 
 * @author Mantas Varnagiris
 */
public class ClearableEditText extends EditText implements OnTouchListener
{
	private Drawable	clear_B;

	public ClearableEditText(Context context)
	{
		super(context);
		init(context, null);
	}

	public ClearableEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public ClearableEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	// EditText
	// --------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		manageClearButton();
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Sets drawable for clear text button. If given drawable is {@code null}, then default drawable will be used.
	 * 
	 * @param drawable
	 *            Drawable for clear text button.
	 */
	public void setClearDrawable(Drawable drawable)
	{
		clear_B = drawable != null ? drawable : getResources().getDrawable(R.drawable.ic_clear);

		// Set bounds of our X button
		clear_B.setBounds(0, 0, clear_B.getIntrinsicWidth(), clear_B.getIntrinsicHeight());
	}

	// Private methods
	// --------------------------------------------------------------------------------------------------------------------------------

	private void init(Context context, AttributeSet attrs)
	{
		// Get attributes
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ClearableEditText, 0, 0);
		try
		{
			final Drawable drawable = a.getDrawable(R.styleable.ClearableEditText_clearDrawable);
			setClearDrawable(drawable);
		}
		finally
		{
			a.recycle();
		}

		// Set OnTouchListener
		setOnTouchListener(this);

		// There may be initial text in the field, so we may need to display the button
		manageClearButton();
	}

	private void manageClearButton()
	{
		if (TextUtils.isEmpty(getText().toString()))
			removeClearButton();
		else
			addClearButton();
	}

	private void addClearButton()
	{
		final Drawable[] compoundDrawables = getCompoundDrawables();
		setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], clear_B, compoundDrawables[3]);
	}

	private void removeClearButton()
	{
		final Drawable[] compoundDrawables = getCompoundDrawables();
		setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], null, compoundDrawables[3]);
	}

	// OnTouchListener
	// --------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// If clear drawable is not visible, don't do anything
		if (getCompoundDrawables()[2] == null)
			return false;

		// We only do something on ACTION_UP
		if (event.getAction() != MotionEvent.ACTION_UP)
			return false;

		// Is touch on our clear button?
		if (event.getX() > getWidth() - getPaddingRight() - clear_B.getIntrinsicWidth())
		{
			setText("");
			removeClearButton();
		}
		return false;
	}
}