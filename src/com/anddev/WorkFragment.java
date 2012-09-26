package com.anddev;

import com.actionbarsherlock.app.SherlockFragment;
import com.anddev.events.WorkEvent;
import com.anddev.events.WorkEventBus;

/**
 * Allows conveniently register for {@link WorkEvents}. You must make sure to implement {@code onEvent()} methods.
 * 
 * @author Mantas Varnagiris
 */
public abstract class WorkFragment extends SherlockFragment
{
	protected final WorkEventBus	workEventBus	= WorkEventBus.getDefault();

	@Override
	public void onResume()
	{
		super.onResume();

		// Register events
		WorkEvent[] workEvents = getEventsToRegister();
		if (workEvents != null)
		{
			for (WorkEvent event : workEvents)
				workEventBus.registerForMainThread(this, event.getClass());
		}
	}

	@Override
	public void onPause()
	{
		// Unregister events
		WorkEvent[] workEvents = getEventsToRegister();
		if (workEvents != null)
		{
			for (WorkEvent event : workEvents)
				workEventBus.unregister(this, event.getClass());
		}

		super.onPause();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * @return An array of {@link WorkEvent} that will be registered in {@link WorkFragment#onResume()} and unregistered in {@link WorkFragment#onPause()}. You
	 *         must make sure to implement {@code onEvent()} methods.
	 */
	protected WorkEvent[] getEventsToRegister()
	{
		return null;
	}

	/**
	 * Called from {@link WorkFragment#onResume()} methods when even is working
	 * <p>
	 * <b>Important:</b> {@link WorkEvent#requestType}, {@link WorkEvent#status}, {@link WorkEvent#errorMessage} will <b>not</b> be set in here so do not do
	 * anything with them here.
	 * </p>
	 * 
	 * @param event
	 *            Event that is already working.
	 */
	protected void onEventWorking(WorkEvent event)
	{
	}
}