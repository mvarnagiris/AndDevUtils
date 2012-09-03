package com.anddev.events;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class WorkEventBus extends EventBus
{
	// Keeps track of currently working tasks
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

		if (workEvent.status == WorkEvent.STATUS_PENDING)
		{
			final Integer count = workingTasks.containsKey(eventId) ? workingTasks.get(eventId) + 1 : 1;
			workingTasks.put(eventId, count);
		}
		else if (workEvent.status != WorkEvent.STATUS_STARTED)
		{
			final Integer count = workingTasks.get(eventId);
			if (count == null || count == 1)
				workingTasks.remove(eventId);
			else
				workingTasks.put(eventId, count - 1);
		}

		post(workEvent);
	}

	public boolean isWorking(String eventId)
	{
		return workingTasks.containsKey(eventId);
	}
}