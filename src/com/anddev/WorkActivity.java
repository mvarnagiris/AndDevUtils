package com.anddev;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.anddev.events.WorkEvent;
import com.anddev.services.WorkService;

/**
 * @author Mantas Varnagiris
 */
public abstract class WorkActivity extends SherlockFragmentActivity
{
	protected int	workingCount	= 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Request feature for progress bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
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
	 */
	protected void onWorkEvent(WorkEvent event)
	{
		if (event.isStarted())
			onWorkStarted();
		else if (event.isFinished())
			onWorkFinished();
	}
}