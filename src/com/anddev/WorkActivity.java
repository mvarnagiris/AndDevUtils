package com.anddev;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.anddev.events.WorkEvent;
import com.anddev.events.WorkEventBus;
import com.anddev.services.WorkService;

/**
 * @author Mantas Varnagiris
 */
public abstract class WorkActivity extends SherlockFragmentActivity
{
	protected final WorkEventBus	workEventBus	= WorkEventBus.getDefault();
	protected int					workingCount	= 0;
	protected WorkEvent[]			progressEvents;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Request feature for progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		// Get progress events
		progressEvents = getProgressEvents();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register progress work events and update progress
		if (progressEvents != null)
		{
			for (WorkEvent workEvent : progressEvents)
			{
				workEventBus.registerForMainThread(this, workEvent.getClass());
				if (workEventBus.isWorking(workEvent.getEventId()))
					onWorkStarted();
			}
		}
	}

	@Override
	protected void onPause()
	{
		// Unregister progress work events
		if (progressEvents != null)
		{
			for (WorkEvent workEvent : progressEvents)
			{
				workEventBus.unregister(this, workEvent.getClass());
				if (workEventBus.isWorking(workEvent.getEventId()))
					onWorkFinished();
			}
		}

		super.onPause();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Increases working tasks counter and makes progress bar visible.
	 */
	protected void onWorkStarted()
	{
		workingCount++;
		setSupportProgressBarIndeterminateVisibility(true);
	}

	/**
	 * Decreases working tasks counter and based on if there are more working tasks or not, changes progress bar visibility.
	 */
	protected void onWorkFinished()
	{
		workingCount--;
		setSupportProgressBarIndeterminateVisibility(workingCount > 0);
	}

	/**
	 * Checks {@link WorkEvent#status} and calls {@link WorkActivity#onWorkStarted()} or {@link WorkActivity#onWorkFinished()} or does nothing.
	 * 
	 * @param event
	 *            Event from {@link WorkService}.
	 */
	protected void onWorkEvent(WorkEvent event)
	{
		if (event.isStarted())
			onWorkStarted();
		else if (event.isFinished())
			onWorkFinished();
	}

	// Abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Create and return an array of work events. This array will be used to mark events that you want to show progress for.
	 * <p>
	 * <b>Important:</b> All instances must have proper {@link WorkEvent#requestType} value. Also you <b>MUST</b> implement {@code onEvent(WorkEvent)} method
	 * for each work event class.
	 * </p>
	 * 
	 * @return Array of {@link WorkEvent} instances.
	 */
	protected abstract WorkEvent[] getProgressEvents();
}