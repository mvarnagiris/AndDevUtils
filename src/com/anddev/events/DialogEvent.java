package com.anddev.events;

import android.app.Dialog;

/**
 * Don't use this class directly. Use subclasses. Only use this class when you want to get notified about all types of dialogs.
 * 
 * @author Mantas Varnagiris
 */
public class DialogEvent
{
	public final int	requestCode;
	public final int	buttonId;

	public DialogEvent(int requestCode, int buttonId)
	{
		this.requestCode = requestCode;
		this.buttonId = buttonId;
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	public int getRequestCode()
	{
		return requestCode;
	}

	public boolean isPositive()
	{
		return buttonId == Dialog.BUTTON_POSITIVE;
	}

	public boolean isNeutral()
	{
		return buttonId == Dialog.BUTTON_NEUTRAL;
	}

	public boolean isNegative()
	{
		return buttonId == Dialog.BUTTON_NEGATIVE;
	}
}