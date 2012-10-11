package com.anddev;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragment;
import com.anddev.WorkActivity.EventToTrack;
import com.anddev.events.WorkEvent;
import com.anddev.events.WorkEventBus;
import com.anddev.services.WorkService;

/**
 * Allows conveniently register for {@link WorkEvents}. You must make sure to implement {@code onEvent()} methods.
 * 
 * @author Mantas Varnagiris
 */
public abstract class WorkFragment extends SherlockFragment
{
	protected final WorkEventBus				workEventBus	= WorkEventBus.getDefault();
	protected final Map<String, EventToTrack>	eventsToTrack	= new HashMap<String, EventToTrack>();

	private static final String					TAG				= "WorkFragment";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Init events to track
		EventToTrack[] eventsToTrackArray = getEventsToTrack();
		if (eventsToTrackArray != null)
			for (EventToTrack eventToTrack : eventsToTrackArray)
				eventsToTrack.put(eventToTrack.event.getEventId(), eventToTrack);

		// Register events
		registerWorkEvents(true);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// Register events
		registerWorkEvents(false);
	}

	@Override
	public void onPause()
	{
		// Unregister events
		unregisterWorkEvents(false);

		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		// Unregister events
		unregisterWorkEvents(true);

		super.onDestroy();
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Create and return an array of work events to track. This array will be used to register for events that you want to track.
	 * <p>
	 * <b>Important:</b> Also you must implement {@code onEvent(WorkEvent)} method for each work event class.
	 * </p>
	 * 
	 * @return Array of {@link EventToTrack} instances.
	 */
	protected EventToTrack[] getEventsToTrack()
	{
		return null;
	}

	/**
	 * Don't call this method. Use {@link WorkFragment#onWorkEvent(WorkEvent)}.
	 * <p>
	 * This method is not private, because it might be useful to override it to get updates about tasks - new events and restored events.
	 * </p>
	 * <p>
	 * Can be called from {@link WorkFragment#onActivityCreated(Bundle)} or {@link WorkFragment#onResume()} when restoring working tasks. If it's called from
	 * there, {@code isRealEvent} will be {@code false}, because it will not have all values set properly.
	 * </p>
	 * 
	 * @param event
	 *            Event. <b>Important: </b>If {@code isRealEvent} is {@code false}, then this will be the event you return from
	 *            {@link WorkFragment#getEventsToTrack()}, so it might not have all properties set.
	 * @param isRealEvent
	 *            {@code false} means that this method was called from {@link WorkFragment#onCreate(Bundle)} or {@link WorkFragment#onResume()} and may not have
	 *            all properties set.
	 */
	protected void onWorkStarted(WorkEvent event, boolean isRealEvent)
	{
	}

	/**
	 * Don't call this method. Use {@link WorkFragment#onWorkEvent(WorkEvent)}.
	 * <p>
	 * This method is not private, because it might be useful to override it to get updates about finished tasks.
	 * </p>
	 * 
	 * @param event
	 *            Event.
	 * @param isRealEvent
	 *            {@code false} means that this method was called from {@link WorkFragment#onDestroy()} or {@link WorkFragment#onPause()} and may not have all
	 *            properties set. It also means that task is still actually working!
	 */
	protected void onWorkFinished(WorkEvent event, boolean isRealEvent)
	{
	}

	/**
	 * Checks {@link WorkEvent#status} and calls {@link WorkFragment#onWorkStarted(WorkEvent, boolean)} or {@link WorkFragment#onWorkFinished(WorkEvent)} or
	 * does nothing.
	 * <p>
	 * <b>Important: </b>You need to make sure to call {@link WorkFragment#onWorkEvent(WorkEvent, boolean)} in {@code onEvent(WorkEvent)} for all events.
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
			onWorkStarted(event, true);
		else if (!eventToTrack.workingOnPending && event.isStarted())
			onWorkStarted(event, true);
		else if (event.isFinished())
			onWorkFinished(event, true);
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
					onWorkStarted(eventToTrack.event, false);
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
					onWorkFinished(eventToTrack.event, false);
			}
		}
	}
}