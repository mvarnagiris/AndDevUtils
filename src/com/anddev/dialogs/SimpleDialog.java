package com.anddev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.greenrobot.event.EventBus;

/**
 * Simple dialog. To get events from this dialog, use {@link EventBus} and register to {@link SimpleDialogEvent}.
 * 
 * @author Mantas Varnagiris
 */
public class SimpleDialog extends SherlockDialogFragment
{
	protected final EventBus	eventBus		= EventBus.getDefault();
	protected int				requestCode		= -1;
	protected String			title			= null;
	protected String			message			= null;
	protected String			positiveButton	= null;
	protected String			neutralButton	= null;
	protected String			negativeButton	= null;
	protected View				customView		= null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		if (!TextUtils.isEmpty(title))
			builder.setTitle(title);

		if (!TextUtils.isEmpty(message))
			builder.setMessage(message);

		if (!TextUtils.isEmpty(positiveButton))
			builder.setPositiveButton(positiveButton, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					eventBus.post(new SimpleDialogEvent(requestCode, Dialog.BUTTON_POSITIVE));
				}
			});

		if (!TextUtils.isEmpty(neutralButton))
			builder.setNeutralButton(neutralButton, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					eventBus.post(new SimpleDialogEvent(requestCode, Dialog.BUTTON_NEUTRAL));
				}
			});

		if (!TextUtils.isEmpty(negativeButton))
			builder.setNegativeButton(negativeButton, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					eventBus.post(new SimpleDialogEvent(requestCode, Dialog.BUTTON_NEGATIVE));
				}
			});

		if (customView != null)
			builder.setView(customView);

		return builder.create();
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Set request code for dialog.
	 * 
	 * @param requestCode
	 *            Request code for dialog.
	 */
	public void setRequestCode(int requestCode)
	{
		this.requestCode = requestCode;
	}

	/**
	 * Set title for dialog.
	 * 
	 * @param title
	 *            Dialog title.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Set message for dialog.
	 * 
	 * @param message
	 *            Dialog message.
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * Set positive button title for dialog.
	 * 
	 * @param positiveButton
	 *            Dialog positive button title.
	 */
	public void setPositiveButton(String positiveButton)
	{
		this.positiveButton = positiveButton;
	}

	/**
	 * Set neutral button title for dialog.
	 * 
	 * @param neutralButton
	 *            Dialog neutral button title.
	 */
	public void setNeutralButton(String neutralButton)
	{
		this.neutralButton = neutralButton;
	}

	/**
	 * Set negative button title for dialog.
	 * 
	 * @param negativeButton
	 *            Dialog negative button title.
	 */
	public void setNegativeButton(String negativeButton)
	{
		this.negativeButton = negativeButton;
	}

	/**
	 * Set custom view for dialog
	 * 
	 * @param customView
	 *            Custom view for dialog.
	 */
	public void setCustomView(View customView)
	{
		this.customView = customView;
	}

	// Builder
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class Builder
	{
		private final int	requestCode;
		private String		title			= null;
		private String		message			= null;
		private String		positiveButton	= null;
		private String		neutralButton	= null;
		private String		negativeButton	= null;
		private View		customView		= null;

		public Builder(int requestCode)
		{
			this.requestCode = requestCode;
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------------

		/**
		 * Creates new {@link SimpleDialog} with arguments provided for this builder.
		 * 
		 * @return
		 */
		public SimpleDialog create()
		{
			SimpleDialog f = new SimpleDialog();

			f.setRequestCode(requestCode);
			f.setTitle(title);
			f.setMessage(message);
			f.setPositiveButton(positiveButton);
			f.setNeutralButton(neutralButton);
			f.setNegativeButton(negativeButton);
			f.setCustomView(customView);

			return f;
		}

		/**
		 * Set title argument for this builder.
		 * 
		 * @param title
		 *            Title argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setTitle(String title)
		{
			this.title = title;
			return this;
		}

		/**
		 * Set message argument for this builder.
		 * 
		 * @param message
		 *            Message argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setMessage(String message)
		{
			this.message = message;
			return this;
		}

		/**
		 * Set positive button title argument for this builder.
		 * 
		 * @param positiveButton
		 *            Positive button title argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setPositiveButton(String positiveButton)
		{
			this.positiveButton = positiveButton;
			return this;
		}

		/**
		 * Set neutral button title argument for this builder.
		 * 
		 * @param neutralButton
		 *            Neutral button title argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setNeutralButton(String neutralButton)
		{
			this.neutralButton = neutralButton;
			return this;
		}

		/**
		 * Set negative button title argument for this builder.
		 * 
		 * @param negativeButton
		 *            Negative button title argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setNegativeButton(String negativeButton)
		{
			this.negativeButton = negativeButton;
			return this;
		}

		/**
		 * Set custom view argument for this builder.
		 * 
		 * @param customView
		 *            Custom view argument for this builder.
		 * @return Same builder for chaining.
		 */
		public Builder setCustomView(View customView)
		{
			this.customView = customView;
			return this;
		}
	}

	// Events
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class SimpleDialogEvent
	{
		public final int	requestCode;
		public final int	buttonId;

		public SimpleDialogEvent(int requestCode, int buttonId)
		{
			this.requestCode = requestCode;
			this.buttonId = buttonId;
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------------

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
}