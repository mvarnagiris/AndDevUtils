package com.anddev.db;

import android.provider.BaseColumns;

public abstract class Table
{
	public static final String	ID	= BaseColumns._ID;

	// Public methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public String getCreateScript()
	{
		return "create table " + getTableName() + " (" + ID + " integer primary key autoincrement, " + getColumnsForCreate() + ");";
	}

	// Public abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public abstract String getTableName();

	// Protected abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	protected abstract String getColumnsForCreate();
}