package com.anddev.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter for swipable fragments.
 * 
 * @author Mantas Varnagiris
 */
public class FragmentCursorPagerAdapter extends FragmentPagerAdapter
{
	protected Context	context;
	protected Cursor	cursor;
	protected int		iId;
	protected Class<?>	fragmentClass;
	protected String	idArgumentName;

	public FragmentCursorPagerAdapter(Context context, FragmentManager fm, Cursor c, Class<?> fragmentClass, String idArgumentName)
	{
		super(fm);
		this.context = context;
		this.cursor = c;
		this.fragmentClass = fragmentClass;
		this.idArgumentName = idArgumentName;
	}

	// FragmentStatePagerAdapter
	// --------------------------------------------------------------------------------------------------------------------------

	public long getItemId(int position)
	{
		cursor.moveToPosition(position);
		return cursor.getLong(iId);
	}

	@Override
	public Fragment getItem(int position)
	{
		cursor.moveToPosition(position);
		final Bundle args = new Bundle();
		args.putLong(idArgumentName, cursor.getLong(iId));
		return Fragment.instantiate(context, fragmentClass.getName(), args);
	}

	@Override
	public int getCount()
	{
		return (cursor != null) ? cursor.getCount() : 0;
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------

	/**
	 * Swaps cursors.
	 * 
	 * @param newCursor
	 *            New Cursor.
	 * @return Old cursor.
	 */
	public Cursor swapCursor(Cursor newCursor)
	{
		if (newCursor != null)
			findIndexes(newCursor);

		final Cursor oldCursor = cursor;
		cursor = newCursor;

		notifyDataSetChanged();

		return oldCursor;
	}

	// Protected methods
	// --------------------------------------------------------------------------------------------------------------------------

	protected void findIndexes(Cursor c)
	{
		iId = c.getColumnIndex(BaseColumns._ID);
	}
}