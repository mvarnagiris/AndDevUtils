package com.anddev.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.anddev.BuildConfig;
import com.anddev.services.WorkServiceV2.WorkEventV2.State;
import com.anddev.utils.PrefsUtils;

import de.greenrobot.event.EventBus;

public abstract class WorkServiceV2 extends IntentService
{
	// Extras you can pass to this service
	public static final String	EXTRA_REQUEST_TYPE	= WorkService.class.getName() + ".EXTRA_REQUEST_TYPE";
	public static final String	EXTRA_FORCE			= WorkService.class.getName() + ".EXTRA_FORCE";

	// Default value used if no EXTRA_REQUEST_TYPE is passed
	public static final int		RT_DEFAULT			= -1;

	protected final String		TAG;

	public WorkServiceV2()
	{
		super("WorkService");
		TAG = getClass().getSimpleName();
	}

	// IntentService
	// -----------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// Get request type
		final int requestType = intent.getIntExtra(EXTRA_REQUEST_TYPE, RT_DEFAULT);

		// Log
		if (BuildConfig.DEBUG)
		{
			final String rtTitle = getTitleForRT(intent, requestType);
			Log.i(TAG, getClass().getSimpleName() + " (RT: " + requestType + ") - Pending" + (!TextUtils.isEmpty(rtTitle) ? ". (" + rtTitle + ")" : ""));
		}

		// Send "pending" event
		final WorkEventV2 workEvent = getWorkEvent(intent, requestType);
		if (workEvent != null)
		{
			final WorkEventV2 currentEvent = (WorkEventV2) EventBus.getDefault().getStickyEvent(workEvent.getClass());
			if (currentEvent == null || currentEvent.isFinished())
			{
				workEvent.requestType = requestType;
				workEvent.state = WorkEventV2.State.PENDING;
				EventBus.getDefault().postSticky(workEvent);
			}
		}

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
		final String prefsKey = PrefsUtils.WorkServicePrefs.getLastSuccessfulWorkTimePrefName(getClass().getName(), requestType,
				getPrefsSuffix(intent, requestType));
		final long lastSuccessfulWorkTime = prefs.getLong(prefsKey, 0);
		final String rtTitle = getTitleForRT(intent, requestType);
		final String className = getClass().getSimpleName();
		WorkEventV2 workEvent;

		try
		{
			// Check if need execute
			needExecute(intent, requestType, startTime, lastSuccessfulWorkTime, force);
		}
		catch (NeedExecuteException e)
		{
			// No need to execute this service

			// Log
			if (BuildConfig.DEBUG)
				Log.i(TAG, className + " (RT: " + requestType + ") - Not executed" + (!TextUtils.isEmpty(rtTitle) ? ". (" + rtTitle + ")" : "") + ". Reason: "
						+ e.getMessage());

			// Send "not executed" event
			workEvent = getWorkEvent(intent, requestType);
			if (workEvent != null)
			{
				workEvent.requestType = requestType;
				workEvent.state = WorkEventV2.State.NOT_EXECUTED;
				EventBus.getDefault().removeStickyEvent(workEvent.getClass());
				EventBus.getDefault().post(workEvent);
			}

			return;
		}

		if (BuildConfig.DEBUG)
			Log.i(TAG, className + " (RT: " + requestType + ") - Started" + (!TextUtils.isEmpty(rtTitle) ? ". (" + rtTitle + ")" : ""));

		// Send "started" event
		workEvent = getWorkEvent(intent, requestType);
		if (workEvent != null)
		{
			final WorkEventV2 currentEvent = (WorkEventV2) EventBus.getDefault().getStickyEvent(workEvent.getClass());
			if (currentEvent == null || currentEvent.getState() != State.STARTED)
			{
				workEvent.requestType = requestType;
				workEvent.state = WorkEventV2.State.STARTED;
				EventBus.getDefault().postSticky(workEvent);
			}
		}

		try
		{
			// Do work
			handleRequest(intent, requestType, startTime, lastSuccessfulWorkTime);

			// Store last refresh time
			PrefsUtils.getPrefs(getApplicationContext()).edit().putLong(prefsKey, startTime).commit();

			if (BuildConfig.DEBUG)
				Log.i(TAG, className + " (RT: " + requestType + ") - Succeeded" + (!TextUtils.isEmpty(rtTitle) ? ". (" + rtTitle + ")" : ""));

			// Send "succeeded" event
			workEvent = getWorkEvent(intent, requestType);
			if (workEvent != null)
			{
				workEvent.requestType = requestType;
				workEvent.state = WorkEventV2.State.SUCCEEDED;
				EventBus.getDefault().removeStickyEvent(workEvent.getClass());
				EventBus.getDefault().post(workEvent);
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, className + " (RT: " + requestType + ") - Failed" + (!TextUtils.isEmpty(rtTitle) ? ". (" + rtTitle + ")" : ""), e);

			// Send "failed" event
			workEvent = getWorkEvent(intent, requestType);
			if (workEvent != null)
			{
				workEvent.requestType = requestType;
				workEvent.state = WorkEventV2.State.FAILED;
				workEvent.exception = e;
				EventBus.getDefault().removeStickyEvent(workEvent.getClass());
				EventBus.getDefault().post(workEvent);
			}
		}
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Create work event for current service.
	 * 
	 * @param requestType
	 *            Request type.
	 * @return Work event with proper request type set
	 */
	protected WorkEventV2 getWorkEvent(Intent intent, int requestType)
	{
		return null;
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
	protected abstract void handleRequest(Intent intent, int requestType, long startTime, long lastSuccessfulWorkTime) throws Exception;

	// Event class
	// -----------------------------------------------------------------------------------------------------------------------------------

	public static class WorkEventV2
	{
		public enum State
		{
			PENDING, STARTED, SUCCEEDED, FAILED, NOT_EXECUTED
		}

		private int			requestType;
		private State		state;
		private Exception	exception	= null;

		public WorkEventV2(int requestType)
		{
			this.requestType = requestType;
		}

		// Public methods
		// -----------------------------------------------------------------------------------------------------------------------------------

		public int getRequestType()
		{
			return requestType;
		}

		public State getState()
		{
			return state;
		}

		public Exception getError()
		{
			return exception;
		}

		public boolean isWorking(boolean isWorkingWhenPending)
		{
			switch (state)
			{
				case PENDING:
					return isWorkingWhenPending;
				case STARTED:
					return true;
				default:
					return false;
			}
		}

		public boolean isFinished()
		{
			switch (state)
			{
				case SUCCEEDED:
				case FAILED:
				case NOT_EXECUTED:
					return true;
				default:
					return false;
			}
		}

		public boolean isSuccessful()
		{
			switch (state)
			{
				case SUCCEEDED:
					return true;
				default:
					return false;
			}
		}
	}

	// Exceptions
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Thrown when work does not need to be executed.
	 * 
	 * @author Mantas Varnagiris
	 */
	protected static class NeedExecuteException extends Exception
	{
		private static final long	serialVersionUID	= 1680639942049548461L;

		public NeedExecuteException(final String reason)
		{
			super(reason);
		}
	}
}