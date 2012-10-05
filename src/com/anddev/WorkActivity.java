package com.anddev;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

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
	protected final WorkEventBus				workEventBus	= WorkEventBus.getDefault();
	protected final Map<String, EventToTrack>	eventsToTrack	= new HashMap<String, EventToTrack>();
	protected int								workingCount	= 0;

	private static final String					TAG				= "WorkActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Request feature for progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		// Init events to track
		EventToTrack[] eventsToTrackArray = getEventsToTrack();
		if (eventsToTrackArray != null)
			for (EventToTrack eventToTrack : eventsToTrackArray)
				eventsToTrack.put(eventToTrack.event.getEventId(), eventToTrack);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register progress work events and update progress.
		EventToTrack eventToTrack;
		for (String eventId : eventsToTrack.keySet())
		{
			eventToTrack = eventsToTrack.get(eventId);
			workEventBus.registerForMainThread(this, eventToTrack.event.getClass());
			if (eventToTrack.showProgress && workEventBus.isWorking(eventId, eventToTrack.workingOnPending))
				onWorkStarted(eventToTrack.event, eventToTrack.showProgress, true);
		}
	}

	@Override
	protected void onPause()
	{
		// Unregister progress work events
		EventToTrack eventToTrack;
		for (String eventId : eventsToTrack.keySet())
		{
			eventToTrack = eventsToTrack.get(eventId);
			workEventBus.unregister(this, eventToTrack.event.getClass());
		}

		// Reset working count and disable progress bar
		workingCount = 0;
		setSupportProgressBarIndeterminateVisibility(false);

		super.onPause();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * If {@code showProgress} is {@code true}, then increases working tasks counter and makes progress bar visible.
	 * <p>
	 * Don't call this method. Use {@link WorkActivity#onWorkEvent(WorkEvent)}.
	 * </p>
	 * <p>
	 * This method is not private, because it might be useful to override it to get updates about tasks - new events and restored events.
	 * </p>
	 * <p>
	 * Can be called from {@link WorkActivity#onResume()} when restoring working tasks. If it's called from there, {@code isRestore} will be {@code true},
	 * because it will not have all values set properly.
	 * </p>
	 * 
	 * @param event
	 *            Event. <b>Important: </b>If {@code isRestore} is {@code true}, then this will be the event you return from
	 *            {@link WorkActivity#getEventsToTrack()}, so it might not have all properties set.
	 * @param showProgress
	 *            Flag that indicates if Activity should show progress bar for this event.
	 * @param isRestore
	 *            {@code true} means that this method was called from {@link WorkActivity#onResume()} and may not have all properties set.
	 */
	protected void onWorkStarted(WorkEvent event, boolean showProgress, boolean isRestore)
	{
		if (showProgress)
		{
			workingCount++;
			setSupportProgressBarIndeterminateVisibility(true);
		}
	}

	/**
	 * Decreases working tasks counter and based on if there are more working tasks or not, changes progress bar visibility.
	 * <p>
	 * Don't call this method. Use {@link WorkActivity#onWorkEvent(WorkEvent)}.
	 * </p>
	 * <p>
	 * This method is not private, because it might be useful to override it to get updates about finished tasks.
	 * </p>
	 * 
	 * @param event
	 *            Event.
	 * @param showProgress
	 *            Flag that indicates if Activity should show progress bar for this event.
	 */
	protected void onWorkFinished(WorkEvent event, boolean showProgress)
	{
		if (showProgress)
		{
			workingCount--;
			setSupportProgressBarIndeterminateVisibility(workingCount > 0);
		}
	}

	/**
	 * Checks {@link WorkEvent#status} and calls {@link WorkActivity#onWorkStarted(WorkEvent, boolean)} or {@link WorkActivity#onWorkFinished(WorkEvent)} or
	 * does nothing.
	 * <p>
	 * <b>Important: </b>You need to make sure to call {@link WorkActivity#onWorkEvent(WorkEvent, boolean)} in {@code onEvent(WorkEvent)} for all events.
	 * </p>
	 * 
	 * @param event
	 *            Event from {@link WorkService}.
	 */
	protected void onWorkEvent(WorkEvent event)
	{
		final EventToTrack eventToTrack = eventsToTrack.get(event.getEventId());

		// This should not happen if implemented correctly
		if (eventToTrack == null)
		{
			if (BuildConfig.DEBUG)
				Log.w(TAG,
						"EventToTrack not found. If you have events that have properties that change eventId, you should check for those properties before calling onWorkEvent(WorkEvent) method. "
								+ event.toString());
			return;
		}

		if (eventToTrack.workingOnPending && event.isPending())
			onWorkStarted(event, eventToTrack.showProgress, false);
		else if (!eventToTrack.workingOnPending && event.isStarted())
			onWorkStarted(event, eventToTrack.showProgress, false);
		else if (event.isFinished())
			onWorkFinished(event, eventToTrack.showProgress);
	}

	/**
	 * Create and return an array of work events to track - get updates about status changes and restore events in {@link WorkActivity#onResume()}. This array
	 * will be used to register for events that you want to track.
	 * <p>
	 * <b>Important:</b> Also you must implement {@code onEvent(WorkEvent)} method for each work event class. You also need to make sure to call
	 * {@link WorkActivity#onWorkEvent(WorkEvent)} in {@code onEvent(WorkEvent)} for all events.
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