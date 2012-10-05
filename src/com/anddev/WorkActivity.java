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
					onWorkStarted(eventToTrack.event, false);
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
	 * <p>
	 * You don't usually call this method directly. Use {@link WorkActivity#onWorkEvent(WorkEvent, boolean)}.
	 * </p>
	 * <p>
	 * Can be called from {@link WorkActivity#onResume()} when restoring working tasks. If it's called from there, {@code isRealEvent} will be {@code false},
	 * because it will not have all values set properly.
	 * </p>
	 * 
	 * @param event
	 *            Event. <b>Important: </b>If {@code isRealEvent} is {@code false}, then {@link WorkEvent#requestType}, {@link WorkEvent#status},
	 *            {@link WorkEvent#errorMessage} will <b>not</b> be set in here so do not do anything with them here.
	 * @param isRealEvent
	 *            {@code true} means that this method was called from {@link WorkActivity#onResume()} and does not have all properties set.
	 */
	protected void onWorkStarted(WorkEvent event, boolean isRealEvent)
	{
		workingCount++;
		setSupportProgressBarIndeterminateVisibility(true);
	}

	/**
	 * Decreases working tasks counter and based on if there are more working tasks or not, changes progress bar visibility.
	 * <p>
	 * You don't usually call this method directly. Use {@link WorkActivity#onWorkEvent(WorkEvent, boolean)}.
	 * </p>
	 * 
	 * @param event
	 *            Event.
	 */
	protected void onWorkFinished(WorkEvent event)
	{
		workingCount--;
		setSupportProgressBarIndeterminateVisibility(workingCount > 0);
	}

	/**
	 * Checks {@link WorkEvent#status} and calls {@link WorkActivity#onWorkStarted(WorkEvent, boolean)} or {@link WorkActivity#onWorkFinished(WorkEvent)} or
	 * does nothing.
	 * <p>
	 * <b>Important: </b>You need to make sure to call {@link WorkActivity#onWorkEvent(WorkEvent, boolean)} in {@code onEvent(WorkEvent)} for events that will
	 * have {@link EventToTrack#showProgress} set to {@code true}.
	 * </p>
	 * 
	 * @param event
	 *            Event from {@link WorkService}.
	 * @param startOnPending
	 *            If {@code true}, event start will be counted on "pending" event.
	 */
	protected void onWorkEvent(WorkEvent event, boolean startOnPending)
	{
		if (startOnPending && event.isPending())
			onWorkStarted(event, true);
		else if (!startOnPending && event.isStarted())
			onWorkStarted(event, true);
		else if (event.isFinished())
			onWorkFinished(event);
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