package com.anddev.services;

import android.content.Intent;
import android.database.Cursor;

import com.anddev.utils.NetworkUtils;

/**
 * Service to use together with {@link SyncTable} to synchronize values with server.
 * 
 * @author Mantas Varnagiris
 */
public abstract class SyncService extends WorkService
{
	public static final String	EXTRA_ITEM_IDS	= SyncService.class.getName() + ".EXTRA_ITEM_IDS";
	public static final String	EXTRA_SYNC_TYPE	= SyncService.class.getName() + ".EXTRA_SYNC_TYPE";

	public static final int		ST_SYNC_ONLY	= 1;
	public static final int		ST_INSERT		= 2;
	public static final int		ST_UPDATE		= 3;
	public static final int		ST_DELETE		= 4;

	@Override
	protected void doWork(Intent intent, int requestType, long startTime, long lastSuccessfulWorkTime) throws Exception
	{
		// Get extras
		final long[] itemIds = intent.getLongArrayExtra(EXTRA_ITEM_IDS);
		final int syncType = intent.getIntExtra(EXTRA_SYNC_TYPE, ST_SYNC_ONLY);

		// Delete or save values if necessary
		if (syncType != ST_SYNC_ONLY)
			saveValues(intent, requestType, syncType, itemIds);

		// If device has network connection, try to push items to server
		if (NetworkUtils.hasNewtorkConnection(getApplicationContext()))
			sync(intent, requestType);
	}

	@Override
	protected void needExecute(Intent intent, int requestType, long startTime, long lastSuccessfulWorkTime, boolean force) throws NeedExecuteException
	{
		// Always
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	protected void sync(Intent intent, int requestType) throws Exception
	{
		final int[] allRequestTypes = getAllRequestTypes();

		Cursor c;
		for (int i = 0; i < allRequestTypes.length; i++)
		{
			c = getCursor(allRequestTypes[i]);
			try
			{
				if (c != null && c.moveToFirst())
				{
					do
					{
						onBeforeItemSync(intent, requestType, c);
						syncItem(intent, requestType, c);
						onAfterItemSync(intent, requestType, c);
					}
					while (c.moveToNext());
				}
			}
			catch (Exception e)
			{
				onFailItemSync(intent, requestType, c);
				throw e;
			}
			finally
			{
				if (c != null && !c.isClosed())
					c.close();
			}
		}
	}

	// Abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Save values to database.
	 * 
	 * @param intent
	 *            Intent with extras.
	 * @param requestType
	 *            Request type from Intent.
	 * @param syncType
	 *            Sync type from Intent.
	 * @param itemIds
	 *            Item IDs from Intent.
	 */
	protected abstract void saveValues(Intent intent, int requestType, int syncType, long[] itemIds) throws Exception;

	/**
	 * @return All request types used in this service.
	 */
	protected abstract int[] getAllRequestTypes();

	/**
	 * Called before item sync. Update all necessary values here.
	 * 
	 * @param intent
	 *            Intent with extras.
	 * @param requestType
	 *            Request type from Intent.
	 * @param c
	 *            Cursor for current request type. Already at the right position.
	 */
	protected abstract void onBeforeItemSync(Intent intent, int requestType, Cursor c);

	/**
	 * Called after item sync. Update all necessary values here.
	 * 
	 * @param intent
	 *            Intent with extras.
	 * @param requestType
	 *            Request type from Intent.
	 * @param c
	 *            Cursor for current request type. Already at the right position.
	 */
	protected abstract void onAfterItemSync(Intent intent, int requestType, Cursor c);

	/**
	 * Called after item sync fails. Update all necessary values here.
	 * 
	 * @param intent
	 *            Intent with extras.
	 * @param requestType
	 *            Request type from Intent.
	 * @param c
	 *            Cursor for current request type. Already at the right position.
	 */
	protected abstract void onFailItemSync(Intent intent, int requestType, Cursor c);

	/**
	 * @param requestType
	 *            Request type.
	 * @return Cursor for given request type.
	 */
	protected abstract Cursor getCursor(int requestType);

	/**
	 * Sync item with the server here.
	 * 
	 * @param intent
	 *            Intent.
	 * @param requestType
	 *            Request type.
	 * @param c
	 *            Cursor is already at the right position.
	 * @throws Exception
	 */
	protected abstract void syncItem(Intent intent, int requestType, Cursor c) throws Exception;

	// Static methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a comma separated string from given array of IDs.
	 * 
	 * @param itemIds
	 *            Array of IDs
	 * @return Comma separated string of IDs.
	 */
	protected static String[] makeStringArray(long[] itemIds)
	{
		final String[] result = new String[itemIds.length];
		for (int i = 0; i < itemIds.length; i++)
		{
			result[i] = String.valueOf(itemIds[i]);
		}

		return result;
	}

	/**
	 * Creates a comma separated placeholders' string of given size.
	 * 
	 * @param itemIds
	 *            Number of placeholders.
	 * @return Comma separated placeholders string.
	 */
	protected static String getCommaSeparatedPlaceholders(int size)
	{
		final StringBuilder commaSeparatedString = new StringBuilder();
		for (int i = 0; i < size; i++)
		{
			if (i > 0)
				commaSeparatedString.append(",");
			commaSeparatedString.append("?");
		}

		return commaSeparatedString.toString();
	}
}