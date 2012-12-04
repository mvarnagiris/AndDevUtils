package com.anddev.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

/**
 * Cursor adapter that puts header view at the top of each section.
 * 
 * @author Mantas Varnagiris
 */
public abstract class AbstractSectionedCursorAdapter extends AbstractCursorAdapter implements SectionIndexer
{
	protected static final int				TYPE_NORMAL	= 0;
	protected static final int				TYPE_HEADER	= 1;

	protected static final int				TYPE_COUNT	= 2;

	protected final String[]				indexColumnNames;
	protected final List<String>			sectionsList;
	protected final Map<Integer, Integer>	sectionToPosition;

	public AbstractSectionedCursorAdapter(Context context, Cursor c, String[] indexColumnNames)
	{
		super(context, c);
		this.indexColumnNames = indexColumnNames;
		this.sectionsList = new ArrayList<String>();
		this.sectionToPosition = new TreeMap<Integer, Integer>();
		prepareIndexer(c);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor)
	{
		if (newCursor != null)
			findIndexes(newCursor);
		prepareIndexer(newCursor);
		return super.swapCursor(newCursor);
	}

	@Override
	public int getCount()
	{
		return super.getCount() + sectionsList.size();
	}

	@Override
	public int getViewTypeCount()
	{
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position == getPositionForSection(getSectionForPosition(position)))
			return TYPE_HEADER;

		return TYPE_NORMAL;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		if (getItemViewType(position) == TYPE_HEADER)
			return false;

		return super.isEnabled(position);
	}

	@Override
	public Object getItem(int position)
	{
		if (getItemViewType(position) == TYPE_NORMAL)
			return super.getItem(position - (getSectionForPosition(position) + 1));

		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final int type = getItemViewType(position);

		// If type is header, set header title
		if (type == TYPE_HEADER)
		{
			mCursor.moveToPosition(getPositionForSection(getSectionForPosition(position)));
			if (convertView == null)
				convertView = newHeaderView(mContext, mCursor, parent);

			bindHeaderView(convertView, mContext, mCursor);
			return convertView;
		}

		// Normal items are handled by subclass
		return super.getView(position - (getSectionForPosition(position) + 1), convertView, parent);
	}

	// Private methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Prepares sections.
	 * 
	 * @param c
	 */
	private void prepareIndexer(Cursor c)
	{
		sectionsList.clear();
		sectionToPosition.clear();

		if (c == null || !c.moveToFirst())
			return;

		final int indexColumnCount = indexColumnNames.length;
		final Set<String> sectionsUniqueIDsSet = new HashSet<String>();
		final int[] iIndexColumns = new int[indexColumnCount];
		for (int i = 0; i < indexColumnCount; i++)
		{
			iIndexColumns[i] = c.getColumnIndex(indexColumnNames[i]);
		}

		int i = 0;
		String[] notParsedSectionValues = new String[indexColumnCount];
		String parsedSectionValue;
		do
		{
			if (sectionsUniqueIDsSet.add(getRowSectionUniqueId(c)))
			{
				for (int e = 0; e < indexColumnCount; e++)
				{
					notParsedSectionValues[e] = c.getString(iIndexColumns[e]);
				}
				parsedSectionValue = parseIndexColumnValue(notParsedSectionValues);
				if (TextUtils.isEmpty(parsedSectionValue))
					parsedSectionValue = "";

				sectionToPosition.put(sectionsList.size(), i + sectionsList.size());
				sectionsList.add(parsedSectionValue);
			}
			i++;
		}
		while (c.moveToNext());
	}

	// Abstract methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	protected abstract String parseIndexColumnValue(String[] indexColumnValues);

	protected abstract String getRowSectionUniqueId(Cursor c);

	protected abstract View newHeaderView(Context context, Cursor c, ViewGroup root);

	protected abstract void bindHeaderView(View view, Context context, Cursor c);

	// SectionIndexer
	// ------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getPositionForSection(int section)
	{
		if (!sectionToPosition.containsKey(section))
			return 0;
		return sectionToPosition.get(section);
	}

	@Override
	public int getSectionForPosition(int position)
	{
		final int sectionsCount = sectionsList.size();

		int i = 0;
		while (i < sectionsCount && getPositionForSection(i) <= position)
			i++;

		i--;
		return i;
	}

	@Override
	public Object[] getSections()
	{
		String[] sections = new String[sectionsList.size()];
		sectionsList.toArray(sections);
		return sections;
	}
}