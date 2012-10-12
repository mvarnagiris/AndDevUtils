package com.anddev.dialogs;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.anddev.events.DialogEvent;

import de.greenrobot.event.EventBus;

public class DateTimeDialog extends SherlockDialogFragment
{
	private static final int	DIALOG_ID_DATE		= 1;
	private static final int	DIALOG_ID_TIME		= 2;

	private static final String	ARG_REQUEST_CODE	= "ARG_REQUEST_CODE";
	private static final String	ARG_DIALOG_ID		= "ARG_DIALOG_ID";
	private static final String	ARG_DATE			= "ARG_DATE";

	public static DateTimeDialog newDateDialogInstance(int requestCode, long timeMillis)
	{
		DateTimeDialog f = new DateTimeDialog();

		final Bundle args = new Bundle();
		args.putInt(ARG_REQUEST_CODE, requestCode);
		args.putInt(ARG_DIALOG_ID, DIALOG_ID_DATE);
		args.putLong(ARG_DATE, timeMillis);
		f.setArguments(args);

		return f;
	}

	public static DateTimeDialog newTimeDialogInstance(int requestCode, long timeMillis)
	{
		DateTimeDialog f = new DateTimeDialog();

		final Bundle args = new Bundle();
		args.putInt(ARG_REQUEST_CODE, requestCode);
		args.putInt(ARG_DIALOG_ID, DIALOG_ID_TIME);
		args.putLong(ARG_DATE, timeMillis);
		f.setArguments(args);

		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Get arguments
		Bundle args = getArguments();

		final int requestCode = args.getInt(ARG_REQUEST_CODE, 0);
		final int dialogId = args.getInt(ARG_DIALOG_ID);
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(args.getLong(ARG_DATE)));

		switch (dialogId)
		{
			case DIALOG_ID_DATE:
				return new DatePickerDialog(getActivity(), new OnDateSetListener()
				{
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
					{
						c.set(Calendar.YEAR, year);
						c.set(Calendar.MONTH, monthOfYear);
						c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						EventBus.getDefault().post(new DateTimeDialogEvent(requestCode, c.getTimeInMillis()));
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

			case DIALOG_ID_TIME:
			{
				return new TimePickerDialog(getActivity(), new OnTimeSetListener()
				{
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute)
					{
						c.set(Calendar.HOUR_OF_DAY, hourOfDay);
						c.set(Calendar.MINUTE, minute);
						EventBus.getDefault().post(new DateTimeDialogEvent(requestCode, c.getTimeInMillis()));
					}
				}, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
			}
		}

		return null;
	}

	// Events
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class DateTimeDialogEvent extends DialogEvent
	{
		private final long	date;

		public DateTimeDialogEvent(int requestCode, long date)
		{
			super(requestCode, Dialog.BUTTON_POSITIVE);
			this.date = date;
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------------

		public long getDate()
		{
			return date;
		}
	}
}