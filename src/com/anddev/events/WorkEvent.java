package com.anddev.events;

import com.anddev.services.WorkService;

/**
 * Event that carries data about {@link WorkService} state.
 * 
 * @author Mantas Varnagiris
 */
public abstract class WorkEvent
{
	public static final int	STATUS_PENDING		= 1;
	public static final int	STATUS_STARTED		= 2;
	public static final int	STATUS_SUCCEEDED	= 3;
	public static final int	STATUS_FAILED		= 4;
	public static final int	STATUS_NOT_EXECUTED	= 5;

	public int				requestType			= -1;
	public int				status				= -1;
	public String			errorMessage		= null;

	public String getEventId()
	{
		return generateEventId(getClass(), requestType);
	}

	@Override
	public String toString()
	{
		return "Request type: " + requestType + ", Status: " + status + ", errorMessage: " + errorMessage;
	}

	// Public methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public boolean isStarted()
	{
		return status == STATUS_PENDING;
	}

	public boolean isFinished()
	{
		return status == STATUS_SUCCEEDED || status == STATUS_NOT_EXECUTED || status == STATUS_FAILED;
	}

	// Static methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	public static String generateEventId(Class<?> cls, int requestType)
	{
		return cls.getName() + "_" + requestType;
	}
}