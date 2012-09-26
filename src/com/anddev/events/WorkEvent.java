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

	public final int		requestType;
	public int				status				= -1;
	public String			errorMessage		= null;

	public WorkEvent(int requestType)
	{
		this.requestType = requestType;
	}

	// Public methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Generates unique event Id for this type of event. All necessary parameters to generate event Id should be passed in constructor.
	 * 
	 * @return Unique event Id.
	 */
	public final String getEventId()
	{
		return WorkEvent.class.getName() + "_" + getEventIdPart();
	}

	/**
	 * @return {@code true} if work is pending.
	 */
	public boolean isPending()
	{
		return status == STATUS_PENDING;
	}

	/**
	 * @return {@code true} if work is started.
	 */
	public boolean isStarted()
	{
		return status == STATUS_STARTED;
	}

	/**
	 * @return {@code true} if work is finished. Can be not executed, succeeded or failed.
	 */
	public boolean isFinished()
	{
		return status == STATUS_SUCCEEDED || status == STATUS_NOT_EXECUTED || status == STATUS_FAILED;
	}

	/**
	 * @return {@code true} if work is succeeded.
	 */
	public boolean isSucceeded()
	{
		return status == STATUS_SUCCEEDED;
	}

	// Abstract methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	/**
	 * @return Suffix of event Id. When extending a class that already extends {@link WorkEvent}, make sure to include super class event Id part like that:
	 *         {@code return super.getEventIdPart() + YourEvent.class.getName() + "_" + maybeSomeId;}, if you want to track super class event work progress.
	 */
	protected abstract String getEventIdPart();
}