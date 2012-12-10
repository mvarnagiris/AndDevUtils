package com.anddev.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static final int				TYPE_NORMAL	= 0;
	public static final int				TYPE_HEADER	= 1;

	protected static final int			TYPE_COUNT	= 2;

	protected final String[]			indexColumnNames;
	protected final List<SectionInfo>	sectionsList;
	protected final boolean				isExpandable;

	public AbstractSectionedCursorAdapter(Context context, Cursor c, String[] indexColumnNames)
	{
		this(context, c, indexColumnNames, false);
	}

	public AbstractSectionedCursorAdapter(Context context, Cursor c, String[] indexColumnNames, boolean isExpandable)
	{
		super(context, c);
		this.indexColumnNames = indexColumnNames;
		this.sectionsList = new ArrayList<SectionInfo>();
		this.isExpandable = isExpandable;
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
		int collapsedSize = 0;
		if (isExpandable)
		{
			for (SectionInfo section : sectionsList)
			{
				if (!section.isExpanded)
					collapsedSize += section.size;
			}
		}

		return super.getCount() + sectionsList.size() - collapsedSize;
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
	public boolean isEnabled(int position)
	{
		if (getItemViewType(position) == TYPE_HEADER)
			return isExpandable ? true : false;

		return super.isEnabled(getCursorPosition(position));
	}

	@Override
	public long getItemId(int position)
	{
		if (getItemViewType(position) == TYPE_NORMAL)
			return super.getItemId(getCursorPosition(position));

		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (getItemViewType(position) == TYPE_NORMAL)
			return super.getItem(getCursorPosition(position));

		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final int type = getItemViewType(position);

		// Header views are handled by subclass
		if (type == TYPE_HEADER)
		{
			final int section = getSectionForPosition(position);
			mCursor.moveToPosition(getCursorPosition(position) + 1);
			if (convertView == null)
				convertView = newHeaderView(mContext, section, mCursor, parent);

			bindHeaderView(convertView, mContext, section, mCursor);
			return convertView;
		}

		// Normal items are handled by parent class
		return super.getView(getCursorPosition(position), convertView, parent);
	}

	// Public methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	public boolean toggleSection(int position)
	{
		if (getItemViewType(position) == TYPE_HEADER)
		{
			final int section = getSectionForPosition(position);
			final int sectionPosition = getPositionForSection(section);
			final SectionInfo sectionInfo = sectionsList.get(section);
			mCursor.moveToPosition(getCursorPosition(sectionPosition) + 1);
			sectionInfo.isExpanded = onToggleSection(section, sectionInfo.isExpanded, mCursor);
			notifyDataSetChanged();
			return true;
		}

		return false;
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
		int size = 0;
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

				final int sectionListSize = sectionsList.size();
				if (sectionListSize > 0)
				{
					final SectionInfo sectionInfo = sectionsList.get(sectionListSize - 1);
					sectionInfo.size = size;
					size = 0;
				}

				final boolean isSectionExpanded = isSectionExpanded(sectionListSize, c);
				sectionsList.add(new SectionInfo(parsedSectionValue, i + sectionsList.size(), isSectionExpanded));
			}
			i++;
			size++;
		}
		while (c.moveToNext());

		final int sectionListSize = sectionsList.size();
		if (sectionListSize > 0)
		{
			final SectionInfo sectionInfo = sectionsList.get(sectionListSize - 1);
			sectionInfo.size = size;
		}
	}

	protected int getCursorPosition(int position)
	{
		final int section = getSectionForPosition(position);
		int collapsedRows = 0;
		if (isExpandable)
		{
			SectionInfo sectionInfo;
			for (int i = 0; i < section; i++)
			{
				sectionInfo = sectionsList.get(i);
				if (!sectionInfo.isExpanded)
					collapsedRows += sectionInfo.size;
			}
		}
		return position - (section + 1) + collapsedRows;
	}

	// Abstract methods
	// ------------------------------------------------------------------------------------------------------------------------------------

	protected abstract boolean isSectionExpanded(int section, Cursor c);

	protected abstract boolean onToggleSection(int section, boolean isExpanded, Cursor c);

	protected abstract String parseIndexColumnValue(String[] indexColumnValues);

	protected abstract String getRowSectionUniqueId(Cursor c);

	protected abstract View newHeaderView(Context context, int section, Cursor c, ViewGroup root);

	protected abstract void bindHeaderView(View view, Context context, int section, Cursor c);

	// SectionIndexer
	// ------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getPositionForSection(int section)
	{
		if (section >= sectionsList.size())
			return 0;

		int collapsedRows = 0;
		if (isExpandable)
		{
			SectionInfo sectionInfo;
			for (int i = 0; i < section; i++)
			{
				sectionInfo = sectionsList.get(i);
				if (!sectionInfo.isExpanded)
					collapsedRows += sectionInfo.size;
			}
		}
		return sectionsList.get(section).position - collapsedRows;
	}

	@Override
	public int getSectionForPosition(int position)
	{
		int section = 0;
		SectionInfo sectionInfo;
		int totalSize = 0;
		for (int i = 0; i < sectionsList.size(); i++)
		{
			sectionInfo = sectionsList.get(i);
			totalSize += 1 + (isExpandable ? sectionInfo.isExpanded ? sectionInfo.size : 0 : 0);

			if (position < totalSize)
				return section;

			section++;
		}

		return section;
	}

	@Override
	public Object[] getSections()
	{
		String[] sections = new String[sectionsList.size()];
		for (int i = 0; i < sectionsList.size(); i++)
			sections[i] = sectionsList.get(i).title;
		return sections;
	}

	// SectionInfo
	// ------------------------------------------------------------------------------------------------------------------------------------

	private static class SectionInfo
	{
		public final String	title;
		public final int	position;
		public boolean		isExpanded;
		public int			size;

		public SectionInfo(String title, int position, boolean isExpanded)
		{
			this.title = title;
			this.position = position;
			this.isExpanded = isExpanded;
			this.size = 0;

		}
	}
}