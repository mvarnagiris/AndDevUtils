package com.anddev;

import com.actionbarsherlock.app.SherlockFragment;
import com.anddev.WorkActivity.EventToTrack;
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
		EventToTrack[] eventsToTrack = getEventsToRegister();
		if (eventsToTrack != null)
		{
			for (EventToTrack eventToTrack : eventsToTrack)
			{
				workEventBus.registerForMainThread(this, eventToTrack.event.getClass());
				if (workEventBus.isWorking(eventToTrack.event.getEventId(), eventToTrack.workingOnPending))
					onEventWorking(eventToTrack.event);
			}
		}
	}

	@Override
	public void onPause()
	{
		// Unregister events
		EventToTrack[] eventsToTrack = getEventsToRegister();
		if (eventsToTrack != null)
		{
			for (EventToTrack eventToTrack : eventsToTrack)
				workEventBus.unregister(this, eventToTrack.event.getClass());
		}

		super.onPause();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * @return An array of {@link EventToTrack} that will be registered in {@link WorkFragment#onResume()} and unregistered in {@link WorkFragment#onPause()}.
	 *         You must make sure to implement {@code onEvent()} methods. {@link EventToTrack#showProgress} field will be ignored.
	 */
	protected EventToTrack[] getEventsToRegister()
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