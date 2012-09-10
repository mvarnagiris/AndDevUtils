package com.anddev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.greenrobot.event.EventBus;

/**
 * Simple dialog with positive and negative buttons. To get events from this dialog, use {@link EventBus} and register to {@link QuestionDialogEvent}.
 * 
 * @author Mantas Varnagiris
 */
public class QuestionDialog extends SherlockDialogFragment
{
	private static final String	ARG_REQUEST_CODE	= "ARG_REQUEST_CODE";
	private static final String	ARG_TITLE			= "ARG_TITLE";
	private static final String	ARG_MESSAGE			= "ARG_MESSAGE";
	private static final String	ARG_POSITIVE		= "ARG_POSITIVE";
	private static final String	ARG_NEGATIVE		= "ARG_NEGATIVE";

	private final EventBus		eventBus			= EventBus.getDefault();

	/**
	 * Creates new instance.
	 * 
	 * @param requestCode
	 *            Used to identify {@link QuestionDialogEvent}.
	 * @param title
	 *            Title for dialog.
	 * @param message
	 *            Message for dialog.
	 * @param positive
	 *            Positive button title.
	 * @param negative
	 *            Negative button title.
	 * @return
	 */
	public static QuestionDialog newInstance(int requestCode, String title, String message, String positive, String negative)
	{
		QuestionDialog f = new QuestionDialog();

		final Bundle args = new Bundle();
		args.putInt(ARG_REQUEST_CODE, requestCode);
		args.putString(ARG_TITLE, title);
		args.putString(ARG_MESSAGE, message);
		args.putString(ARG_POSITIVE, positive);
		args.putString(ARG_NEGATIVE, negative);
		f.setArguments(args);

		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final int requestCode;
		final String title;
		final String message;
		final String positive;
		final String negative;

		// Get arguments
		final Bundle args = getArguments();
		if (args != null)
		{
			requestCode = args.getInt(ARG_REQUEST_CODE);
			title = args.getString(ARG_TITLE);
			message = args.getString(ARG_MESSAGE);
			positive = args.getString(ARG_POSITIVE);
			negative = args.getString(ARG_NEGATIVE);
		}
		else
		{
			requestCode = -1;
			title = null;
			message = null;
			positive = "Yes";
			negative = "No";
		}

		// Build dialog
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (!TextUtils.isEmpty(title))
			builder.setTitle(title);
		if (!TextUtils.isEmpty(message))
			builder.setMessage(message);
		builder.setPositiveButton(positive, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				eventBus.post(new QuestionDialogEvent(requestCode, true));
			}
		});
		builder.setNegativeButton(negative, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				eventBus.post(new QuestionDialogEvent(requestCode, false));
			}
		});

		return builder.create();
	}

	// Events
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class QuestionDialogEvent
	{
		public final int		requestCode;
		public final boolean	isPositive;

		public QuestionDialogEvent(int requestCode, boolean isPositive)
		{
			this.requestCode = requestCode;
			this.isPositive = isPositive;
		}
	}
}