package com.anddev.events;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class WorkEventBus extends EventBus
{
	// Keeps track of currently working/pending tasks
	protected final Map<String, Integer>	pendingTasks	= new HashMap<String, Integer>();
	protected final Map<String, Integer>	workingTasks	= new HashMap<String, Integer>();

	private static WorkEventBus				instance		= null;

	public static WorkEventBus getDefault()
	{
		if (instance == null)
			instance = new WorkEventBus();
		return instance;
	}

	// Public methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public void postWork(WorkEvent workEvent)
	{
		final String eventId = workEvent.getEventId();
		final int status = workEvent.status;

		if (status == WorkEvent.STATUS_PENDING)
		{
			final Integer count = pendingTasks.containsKey(eventId) ? pendingTasks.get(eventId) + 1 : 1;
			pendingTasks.put(eventId, count);
		}
		else if (status == WorkEvent.STATUS_STARTED)
		{
			// Remove from pending tasks
			Integer count = pendingTasks.get(eventId);
			if (count == null || count == 1)
				pendingTasks.remove(eventId);
			else
				pendingTasks.put(eventId, count - 1);

			// Add to working tasks
			count = workingTasks.containsKey(eventId) ? workingTasks.get(eventId) + 1 : 1;
			workingTasks.put(eventId, count);
		}
		else
		{
			final Integer count = workingTasks.get(eventId);
			if (count == null || count == 1)
				workingTasks.remove(eventId);
			else
				workingTasks.put(eventId, count - 1);
		}

		post(workEvent);
	}

	public boolean isWorking(String eventId, boolean workingOnPending)
	{
		// Check pending tasks if necessary
		if (workingOnPending)
			for (String key : pendingTasks.keySet())
				if (key.startsWith(eventId))
					return true;

		// Check working tasks
		for (String key : workingTasks.keySet())
			if (key.startsWith(eventId))
				return true;

		return false;
	}
}