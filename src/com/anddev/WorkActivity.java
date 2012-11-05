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
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		setSupportProgressBarIndeterminateVisibility(false);

		// Init events to track
		EventToTrack[] eventsToTrackArray = getEventsToTrack();
		if (eventsToTrackArray != null)
			for (EventToTrack eventToTrack : eventsToTrackArray)
				eventsToTrack.put(eventToTrack.event.getEventId(), eventToTrack);

		// Register events
		registerWorkEvents(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Register events
		registerWorkEvents(false);
	}

	@Override
	protected void onPause()
	{
		// Unregister events
		unregisterWorkEvents(false);

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		// Unregister events
		unregisterWorkEvents(true);

		super.onDestroy();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Create and return an array of work events to track - get updates about status changes and restore events in {@link WorkActivity#onResume()}. This array
	 * will be used to register for events that you want to track.
	 * <p>
	 * <b>Important:</b> Also you must implement {@code onEvent(WorkEvent)} method for each work event class. You also need to make sure to call
	 * {@link WorkActivity#onWorkEvent(WorkEvent)} in {@code onEvent(WorkEvent)} for all events.
	 * </p>
	 * 
	 * @return Array of {@link EventToTrack} instances.
	 */
	protected EventToTrack[] getEventsToTrack()
	{
		return null;
	}

	/**
	 * If {@code showProgress} is {@code true}, then increases working tasks counter and makes progress bar visible.
	 * <p>
	 * Don't call this method. Use {@link WorkActivity#onWorkEvent(WorkEvent)}.
	 * </p>
	 * <p>
	 * This method is not private, because it might be useful to override it to get updates about tasks - new events and restored events.
	 * </p>
	 * <p>
	 * Can be called from {@link WorkActivity#onCreate(Bundle)} or {@link WorkActivity#onResume()} when restoring working tasks. If it's called from there,
	 * {@code isRealEvent} will be {@code false}, because it will not have all values set properly.
	 * </p>
	 * 
	 * @param event
	 *            Event. <b>Important: </b>If {@code isRealEvent} is {@code false}, then this will be the event you return from
	 *            {@link WorkActivity#getEventsToTrack()}, so it might not have all properties set.
	 * @param showProgress
	 *            Flag that indicates if Activity should show progress bar for this event.
	 * @param isRealEvent
	 *            {@code false} means that this method was called from {@link WorkActivity#onCreate(Bundle)} or {@link WorkActivity#onResume()} and may not have
	 *            all properties set.
	 */
	protected void onWorkStarted(WorkEvent event, boolean showProgress, boolean isRealEvent)
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
	 * @param isRealEvent
	 *            {@code false} means that this method was called from {@link WorkActivity#onDestroy()} or {@link WorkActivity#onPause()} and may not have all
	 *            properties set. It also means that task is still actually working!
	 */
	protected void onWorkFinished(WorkEvent event, boolean showProgress, boolean isRealEvent)
	{
		if (showProgress)
		{
			workingCount--;
			setSupportProgressBarIndeterminateVisibility(workingCount > 0);
		}
	}

	/**
	 * Checks {@link WorkEvent#status} and calls {@link WorkActivity#onWorkStarted(WorkEvent, boolean, boolean)} or
	 * {@link WorkActivity#onWorkFinished(WorkEvent)} or does nothing.
	 * <p>
	 * <b>Important: </b>You need to make sure to call {@link WorkActivity#onWorkEvent(WorkEvent, boolean, boolean)} in {@code onEvent(WorkEvent)} for all
	 * events.
	 * </p>
	 * 
	 * @param event
	 *            Event from {@link WorkService}.
	 */
	protected void onWorkEvent(WorkEvent event)
	{
		boolean eventsFound = false;
		for (String eventId : eventsToTrack.keySet())
		{
			if (event.getEventId().startsWith(eventId))
			{
				eventsFound = true;

				final EventToTrack eventToTrack = eventsToTrack.get(eventId);

				if (eventToTrack.workingOnPending && event.isPending())
					onWorkStarted(event, eventToTrack.showProgress, true);
				else if (!eventToTrack.workingOnPending && event.isStarted())
					onWorkStarted(event, eventToTrack.showProgress, true);
				else if (event.isFinished())
					onWorkFinished(event, eventToTrack.showProgress, true);
			}
		}

		// This should not happen if implemented correctly
		if (!eventsFound)
		{
			if (BuildConfig.DEBUG)
				Log.w(TAG,
						"EventToTrack not found. If you have events that have properties that change eventId, you should check for those properties before calling onWorkEvent(WorkEvent) method. "
								+ event.toString());
			return;
		}

	}

	// Private methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	private void registerWorkEvents(boolean isOnCreate)
	{
		EventToTrack eventToTrack;
		for (String eventId : eventsToTrack.keySet())
		{
			eventToTrack = eventsToTrack.get(eventId);
			if (isOnCreate && eventToTrack.trackOnCreate || !isOnCreate && !eventToTrack.trackOnCreate)
			{
				workEventBus.registerForMainThread(this, eventToTrack.event.getClass());
				if (workEventBus.isWorking(eventId, eventToTrack.workingOnPending))
					onWorkStarted(eventToTrack.event, eventToTrack.showProgress, false);
			}
		}
	}

	private void unregisterWorkEvents(boolean isOnDestroy)
	{
		EventToTrack eventToTrack;
		for (String eventId : eventsToTrack.keySet())
		{
			eventToTrack = eventsToTrack.get(eventId);
			if (isOnDestroy && eventToTrack.trackOnCreate || !isOnDestroy && !eventToTrack.trackOnCreate)
			{
				workEventBus.unregister(this, eventToTrack.event.getClass());
				if (workEventBus.isWorking(eventId, eventToTrack.workingOnPending))
					onWorkFinished(eventToTrack.event, eventToTrack.showProgress, false);
			}
		}
	}

	// EventToTrack
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Holds information about progress tracking for event.
	 * 
	 * @author Mantas Varnagiris
	 */
	public static class EventToTrack
	{
		public final WorkEvent	event;
		public final boolean	workingOnPending;
		public final boolean	showProgress;
		public final boolean	trackOnCreate;

		/**
		 * @param event
		 *            Event that has correct eventId.
		 * @param startsOnPending
		 *            If {@code true}, then event will be considered as working on "pending" status.
		 * @param showProgress
		 *            If {@code true} then progress bar will be shown in ActionBar for this event when it's working.
		 * @param trackOnCreate
		 *            If {@code true} the event will be registered and restored in {@link WorkActivity#onCreate(Bundle)} and unregistered in
		 *            {@link WorkActivity#onDestroy()}; otherwise these methods will be used accordingly - {@link WorkActivity#onResume()} and
		 *            {@link WorkActivity#onPause()}.
		 */
		public EventToTrack(WorkEvent event, boolean startsOnPending, boolean showProgress, boolean trackOnCreate)
		{
			this.event = event;
			this.workingOnPending = startsOnPending;
			this.showProgress = showProgress;
			this.trackOnCreate = trackOnCreate;
		}
	}
}