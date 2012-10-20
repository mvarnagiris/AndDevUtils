package com.anddev.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;

/**
 * Abstract parser class. Takes care of parsing and storing data.
 * 
 * @author Mantas Varnagiris
 */
public abstract class Parser
{
	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------

	/**
	 * Parses values.
	 * 
	 * @param info
	 *            Object to parse.
	 * @return Object that contains parsed values.
	 * @throws JSONException
	 */
	public ParsedValues parse(Context context, Object info) throws Exception
	{
		final ParsedValues parsedValues = new ParsedValues();
		parse(context, parsedValues, info);
		return parsedValues;
	}

	/**
	 * Parses values and persists them.
	 * 
	 * @param context
	 * @param info
	 * @return
	 * @throws JSONException
	 */
	public ParsedValues parseAndStore(Context context, Object info) throws Exception
	{
		ParsedValues parsedValues = parse(context, info);
		store(context, parsedValues);
		return parsedValues;
	}

	// Abstract methods
	// --------------------------------------------------------------------------------------------------------------------------

	/**
	 * Parses values and puts them in {@link ParsedValues} object.
	 * 
	 * @param parsedValues
	 *            Put parsed values here.
	 * @param info
	 *            Object to parse.
	 * @throws JSONException
	 */
	protected abstract void parse(Context context, ParsedValues parsedValues, Object info) throws Exception;

	/**
	 * Persists values.
	 * 
	 * @param context
	 *            Context.
	 * @param parsedValues
	 *            Parsed values.
	 */
	public abstract void store(Context context, ParsedValues parsedValues);

	// ParsedValues
	// --------------------------------------------------------------------------------------------------------------------------

	/**
	 * Object that contains parsed {@link ContentValues} arrays and single objects.
	 * 
	 * @author Mantas Varnagiris
	 */
	public static class ParsedValues
	{
		public final Map<String, ContentValues>		parsedObjectsMap;
		public final Map<String, ContentValues[]>	parsedArraysMap;

		public ParsedValues()
		{
			parsedObjectsMap = new HashMap<String, ContentValues>();
			parsedArraysMap = new HashMap<String, ContentValues[]>();
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------

		public void putObject(String key, ContentValues values)
		{
			parsedObjectsMap.put(key, values);
		}

		public void putArray(String key, ContentValues[] valuesArray)
		{
			parsedArraysMap.put(key, valuesArray);
		}

		/**
		 * Converts list to array and calls {@link ParsedValues#putArray(String, ContentValues[])}.
		 * 
		 * @param key
		 * @param valuesList
		 */
		public void putList(String key, List<ContentValues> valuesList)
		{
			ContentValues[] valuesArray = new ContentValues[valuesList.size()];
			valuesList.toArray(valuesArray);
			putArray(key, valuesArray);
		}

		public ContentValues getObject(String key)
		{
			return parsedObjectsMap.get(key);
		}

		public ContentValues[] getArray(String key)
		{
			return parsedArraysMap.get(key);
		}

		public boolean hasObject(String key)
		{
			return parsedObjectsMap.containsKey(key);
		}

		public boolean hasArray(String key)
		{
			return parsedArraysMap.containsKey(key);
		}
	}
}