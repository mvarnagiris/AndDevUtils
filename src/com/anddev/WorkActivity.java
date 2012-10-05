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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Request feature for progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register progress work events and update progress
		final EventToTrack[] eventsToTrack = getEventsToTrack();
		if (eventsToTrack != null)
		{
			for (EventToTrack eventToTrack : eventsToTrack)
			{
				workEventBus.registerForMainThread(this, eventToTrack.event.getClass());
				if (eventToTrack.showProgress && workEventBus.isWorking(eventToTrack.event.getEventId(), eventToTrack.workingOnPending))
				{
					onWorkStarted();
					onEventWorking(eventToTrack.event);
				}
			}
		}
	}

	@Override
	protected void onPause()
	{
		// Unregister progress work events
		final EventToTrack[] eventsToTrack = getEventsToTrack();
		if (eventsToTrack != null)
		{
			for (EventToTrack eventToTrack : eventsToTrack)
			{
				workEventBus.unregister(this, eventToTrack.event.getClass());
			}
		}

		// Reset working count
		workingCount = 0;
		setSupportProgressBarIndeterminateVisibility(false);

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
	 * @param startOnPending
	 *            If {@code true}, event start will be counted on "pending" event.
	 */
	protected void onWorkEvent(WorkEvent event, boolean startOnPending)
	{
		if (startOnPending && event.isPending())
			onWorkStarted();
		else if (!startOnPending && event.isStarted())
			onWorkStarted();
		else if (event.isFinished())
			onWorkFinished();
	}

	/**
	 * Called from {@link WorkActivity#onResume()} methods when even is working
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

	/**
	 * Create and return an array of work events to track. This array will be used to register for events that you want to track.
	 * <p>
	 * <b>Important:</b> Also you must implement {@code onEvent(WorkEvent)} method for each work event class. You also need to make sure to call
	 * {@link WorkActivity#onWorkEvent(WorkEvent, boolean)} in {@code onEvent(WorkEvent)} for events that will have {@link EventToTrack#showProgress} set to
	 * {@code true}.
	 * </p>
	 * 
	 * @return Array of {@link WorkEvent} instances.
	 */
	protected EventToTrack[] getEventsToTrack()
	{
		return null;
	}

	// EventToTrack
	// -----------------------------------------------------------------------------------------------------------------------------------

	public static class EventToTrack
	{
		public final WorkEvent	event;
		public final boolean	workingOnPending;
		public final boolean	showProgress;

		public EventToTrack(WorkEvent event, boolean startsOnPending, boolean showProgress)
		{
			this.event = event;
			this.workingOnPending = startsOnPending;
			this.showProgress = showProgress;
		}
	}
}