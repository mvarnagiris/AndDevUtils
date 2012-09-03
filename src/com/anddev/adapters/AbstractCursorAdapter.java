package com.anddev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;

/**
 * Takes care of finding cursor columns' indexes.
 * 
 * @author Mantas Varnagiris
 */
public abstract class AbstractCursorAdapter extends CursorAdapter
{
	public AbstractCursorAdapter(Context context, Cursor c)
	{
		super(context, c, true);
		if (c != null)
			findIndexes(c);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor)
	{
		if (newCursor != null)
			findIndexes(newCursor);
		return super.swapCursor(newCursor);
	}

	// Abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Get columns' indexes here.
	 * 
	 * @param c
	 *            Cursor.
	 */
	protected abstract void findIndexes(Cursor c);
}