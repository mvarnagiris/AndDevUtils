package com.anddev.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.anddev.BuildConfig;
import com.anddev.events.WorkEvent;
import com.anddev.events.WorkEventBus;
import com.anddev.utils.PrefsUtils;

/**
 * Service to do any work in background. Notifies about work state.
 * 
 * @author Mantas Varnagiris
 */
public abstract class WorkService extends IntentService
{
	// Extras you can pass to this service
	public static final String			EXTRA_REQUEST_TYPE	= WorkService.class.getName() + ".EXTRA_REQUEST_TYPE";
	public static final String			EXTRA_FORCE			= WorkService.class.getName() + ".EXTRA_FORCE";

	// Default value used if no EXTRA_REQUEST_TYPE is passed
	public static final int				RT_DEFAULT			= -1;

	protected static final WorkEventBus	workEventBus		= WorkEventBus.getDefault();

	protected final String				TAG;

	public WorkService()
	{
		super("WorkServicePrefs");
		TAG = getClass().getSimpleName();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// Get request type
		final int requestType = intent.getIntExtra(EXTRA_REQUEST_TYPE, RT_DEFAULT);

		if (BuildConfig.DEBUG)
		{
			final String rtTitle = getTitleForRT(intent, requestType);
			Log.i(TAG, getClass().getSimpleName() + " - pending. RT: " + requestType + (!TextUtils.isEmpty(rtTitle) ? "(" + rtTitle + ")" : ""));
		}

		// Send "pending" event
		final WorkEvent workEvent = getWorkEvent(intent, requestType);
		workEvent.requestType = requestType;
		workEvent.status = WorkEvent.STATUS_PENDING;
		onWorkPending(intent, requestType, workEvent);
		workEventBus.postWork(workEvent);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		// Init and get extras
		final long startTime = System.currentTimeMillis();
		final int requestType = intent.getIntExtra(EXTRA_REQUEST_TYPE, RT_DEFAULT);
		final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
		final SharedPreferences prefs = PrefsUtils.getPrefs(getApplicationContext());
		WorkEvent workEvent;
		final String prefsKey = PrefsUtils.WorkServicePrefs.getLastSuccessfulWorkTimePrefName(getClass().getName(), requestType,
				getPrefsSuffix(intent, requestType));
		final long lastSuccessfulWorkTime = prefs.getLong(prefsKey, 0);
		final String rtTitle = getTitleForRT(intent, requestType);
		final String className = getClass().getSimpleName();

		try
		{
			// Check if need execute
			needExecute(intent, requestType, startTime, lastSuccessfulWorkTime, force);
		}
		catch (NeedExecuteException e)
		{
			// No need to execute this service

			if (BuildConfig.DEBUG)
				Log.i(TAG,
						className + " - not executed. RT: " + requestType + (!TextUtils.isEmpty(rtTitle) ? "(" + rtTitle + ")" : "") + ". Reason: "
								+ e.getMessage());

			// Send "not executed" event
			workEvent = getWorkEvent(intent, requestType);
			workEvent.requestType = requestType;
			workEvent.status = WorkEvent.STATUS_NOT_EXECUTED;
			onWorkNotExecuted(intent, requestType, workEvent, startTime);
			workEventBus.postWork(workEvent);

			return;
		}

		if (BuildConfig.DEBUG)
			Log.i(TAG, className + " - started. RT: " + requestType + (!TextUtils.isEmpty(rtTitle) ? "(" + rtTitle + ")" : ""));

		// Send "started" event
		workEvent = getWorkEvent(intent, requestType);
		workEvent.requestType = requestType;
		workEvent.status = WorkEvent.STATUS_STARTED;
		onWorkStarted(intent, requestType, workEvent, startTime);
		workEventBus.postWork(workEvent);

		try
		{
			// Do work
			doWork(intent, requestType, startTime, lastSuccessfulWorkTime);

			// Store last refresh time
			PrefsUtils.getPrefs(getApplicationContext()).edit().putLong(prefsKey, startTime).commit();

			if (BuildConfig.DEBUG)
				Log.i(TAG, className + " - succeeded. RT: " + requestType + (!TextUtils.isEmpty(rtTitle) ? "(" + rtTitle + ")" : ""));

			// Send "succeeded" event
			workEvent = getWorkEvent(intent, requestType);
			workEvent.requestType = requestType;
			workEvent.status = WorkEvent.STATUS_SUCCEEDED;
			onWorkSucceeded(intent, requestType, workEvent, startTime);
			workEventBus.postWork(workEvent);
		}
		catch (Exception e)
		{
			Log.e(TAG, className + " - failed. RT: " + requestType + (!TextUtils.isEmpty(rtTitle) ? "(" + rtTitle + ")" : ""));

			// Send "failed" broadcast
			workEvent = getWorkEvent(intent, requestType);
			workEvent.requestType = requestType;
			workEvent.status = WorkEvent.STATUS_FAILED;
			workEvent.errorMessage = e.getMessage();
			onWorkFailed(intent, requestType, workEvent, startTime, e);
			workEventBus.postWork(workEvent);
		}
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Called before sending event. When work is pending.
	 * 
	 * @param intent
	 *            Intent that came with service start
	 * @param requestType
	 *            Request type. For convenience
	 * @param workEvent
	 *            Work event to modify if necessary
	 */
	protected void onWorkPending(Intent intent, int requestType, WorkEvent workEvent)
	{
	}

	/**
	 * Called before sending event. When work is not executed.
	 * 
	 * @param intent
	 *            Intent that came with service start
	 * @param requestType
	 *            Request type. For convenience
	 * @param workEvent
	 *            Work event to modify if necessary
	 * @param startTime
	 *            Time when service started handling this intent.
	 */
	protected void onWorkNotExecuted(Intent intent, int requestType, WorkEvent workEvent, long startTime)
	{
	}

	/**
	 * Called before sending event. When work has started.
	 * 
	 * @param intent
	 *            Intent that came with service start
	 * @param requestType
	 *            Request type. For convenience
	 * @param workEvent
	 *            Work event to modify if necessary
	 * @param startTime
	 *            Time when service started handling this intent.
	 */
	protected void onWorkStarted(Intent intent, int requestType, WorkEvent workEvent, long startTime)
	{
	}

	/**
	 * Called before sending event. When work failed.
	 * 
	 * @param intent
	 *            Intent that came with service start
	 * @param requestType
	 *            Request type. For convenience
	 * @param workEvent
	 *            Work event to modify if necessary
	 * @param startTime
	 *            Time when service started handling this intent.
	 * @param e
	 *            Exception. For convenience.
	 */
	protected void onWorkFailed(Intent intent, int requestType, WorkEvent workEvent, long startTime, Exception e)
	{
	}

	/**
	 * Called before sending event. When work succeeded.
	 * 
	 * @param intent
	 *            Intent that came with service start
	 * @param requestType
	 *            Request type. For convenience
	 * @param workEvent
	 *            Work event to modify if necessary
	 * @param startTime
	 *            Time when service started handling this intent.
	 */
	protected void onWorkSucceeded(Intent intent, int requestType, WorkEvent workEvent, long startTime)
	{
	}

	/**
	 * Called when building key to get/store last successful work time.
	 * <p>
	 * Note: You don't need to include request type, because it's already included in the name.
	 * </p>
	 * 
	 * @param intent
	 *            Intent.
	 * @param requestType
	 *            Request type.
	 * @return Suffix for preferences key. Can be {@code null}.
	 */
	protected String getPrefsSuffix(Intent intent, int requestType)
	{
		return "";
	}

	/**
	 * Determine if work needs to be done or not.
	 * 
	 * @param intent
	 *            Intent passed to service.
	 * @param requestType
	 *            Request type from intent. For convenience.
	 * @param startTime
	 *            Time when service started handling this intent.
	 * @param force
	 *            Force value from intent. For convenience.
	 * @throws NeedExecuteException
	 *             When work does not need to be done.
	 */
	protected void needExecute(Intent intent, int requestType, long startTime, long lastSuccessfulWokTime, boolean force) throws NeedExecuteException
	{
	}

	/**
	 * Override this method and return title for request type for better logging.
	 * 
	 * @param intent
	 *            Intent passed to service.
	 * @param requestType
	 *            Request type
	 * @return Title for request type, or {@code null}.
	 */
	protected String getTitleForRT(Intent intent, int requestType)
	{
		return null;
	}

	// Abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Do all service work here. It's called from {@link AbstractService#onHandleIntent(Intent)}.
	 * 
	 * @param intent
	 *            Intent passed to service.
	 * @param requestType
	 *            Request type from intent. For convenience.
	 * @param startTime
	 *            Time when service started handling this intent.
	 * @throws Exception
	 */
	protected abstract void doWork(Intent intent, int requestType, long startTime, long lastSuccessfulWorkTime) throws Exception;

	/**
	 * Create work event for current service.
	 * 
	 * @param requestType
	 *            Request type.
	 * @return Work event with proper request type set
	 */
	protected abstract WorkEvent getWorkEvent(Intent intent, int requestType);

	// Exceptions
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Thrown when work does not need to be executed.
	 * 
	 * @author Mantas Varnagiris
	 */
	protected static class NeedExecuteException extends Exception
	{
		private static final long	serialVersionUID	= 4201215821970180238L;

		public NeedExecuteException(final String reason)
		{
			super(reason);
		}
	}
}