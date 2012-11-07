package com.anddev.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * Cursor adapter that puts header view at the top of each section.
 * 
 * @author Mantas Varnagiris
 */
public abstract class AbstractSectionedCursorAdapter extends AbstractCursorAdapter implements SectionIndexer
{
	protected static final int		TYPE_HEADER	= 1;
	protected static final int		TYPE_NORMAL	= 0;

	private static final int		TYPE_COUNT	= 2;

	private final String			indexColumnName;
	protected final int				headerLayoutId;
	protected final int				headerTextViewId;
	private String[]				sections;
	private Map<Integer, Integer>	sectionToPosition;

	public AbstractSectionedCursorAdapter(Context context, Cursor c, String indexColumnName, int headerLayoutId, int headerTextViewId)
	{
		super(context, c);
		this.indexColumnName = indexColumnName;
		this.headerLayoutId = headerLayoutId;
		this.headerTextViewId = headerTextViewId;
		prepareIndexer(c);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor)
	{
		prepareIndexer(newCursor);
		return super.swapCursor(newCursor);
	}

	@Override
	public int getCount()
	{
		if (super.getCount() != 0)
			return super.getCount() + (sections == null ? 0 : sections.length);

		return 0;
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

		return true;
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
			if (convertView == null)
				convertView = LayoutInflater.from(mContext).inflate(headerLayoutId, parent, false);

			final TextView header_TV = (TextView) convertView.findViewById(headerTextViewId);

			header_TV.setText((String) getSections()[getSectionForPosition(position)]);
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
		sectionToPosition = new TreeMap<Integer, Integer>();

		ArrayList<String> sectionsList = new ArrayList<String>();
		Set<String> sectionsSet = new HashSet<String>();

		if (c == null || !c.moveToFirst())
			return;

		final int iIndexColumn = c.getColumnIndexOrThrow(indexColumnName);

		int i = 0;
		do
		{

			String sectionValue = c.getString(iIndexColumn);
			final String parsedSectionValue = parseIndexColumnValue(sectionValue);
			if (!TextUtils.isEmpty(parsedSectionValue))
				sectionValue = parsedSectionValue;

			if (sectionsSet.add(sectionValue))
			{
				sectionToPosition.put(sectionsList.size(), i + sectionsList.size());
				sectionsList.add(sectionValue);
			}
			i++;
		}
		while (c.moveToNext());

		sections = new String[sectionsList.size()];
		sectionsList.toArray(sections);
	}

	// Abstract methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * You can parse the value for header here. If you do not want to parse, just return the same value or {@code null}.
	 * 
	 * @param indexColumnValue
	 *            Value to parse.
	 * @return Parsed/Same value or {@code null}. {@code null} and empty values will be ignored.
	 */
	protected abstract String parseIndexColumnValue(String indexColumnValue);

	// SectionIndexer
	// ------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getPositionForSection(int section)
	{
		return sectionToPosition.get(section);
	}

	@Override
	public int getSectionForPosition(int position)
	{
		final int sectionsCount = sections.length;
		int i = 0;

		while (i < sectionsCount && getPositionForSection(i) <= position)
			i++;

		i--;
		return i;
	}

	@Override
	public Object[] getSections()
	{
		return sections;
	}
}
