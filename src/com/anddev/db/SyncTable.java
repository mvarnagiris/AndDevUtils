package com.anddev.db;

public abstract class SyncTable extends Table
{
	public static final int	SYNC_STATE_NOT_SYNCED	= 1;
	public static final int	SYNC_STATE_SYNCING		= 2;
	public static final int	SYNC_STATE_SYNCED		= 3;

	// Table
	// -----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public String getCreateScript()
	{
		return "create table " + getTableName() + " (" + ID + " integer primary key autoincrement, " + getColumnDeleted() + " boolean default 0, "
						+ getColumnSyncState() + " integer default " + SYNC_STATE_NOT_SYNCED + ", " + getColumnsForCreate() + ");";
	}

	// Public methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public String getColumnDeleted()
	{
		return getTableName() + "_" + "deleted";
	}

	public String getColumnSyncState()
	{
		return getTableName() + "_" + "sync_state";
	}
}