package com.anddev;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

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

		// Setup ActionBar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	// Protected methods
	// -----------------------------------------------------------------------------------------------------------------------------------

	protected void onWorkStarted()
	{
		workingCount++;
		setSupportProgressBarIndeterminateVisibility(true);
	}

	protected void onWorkFinished()
	{
		workingCount--;
		setSupportProgressBarIndeterminateVisibility(workingCount > 0);
	}
}