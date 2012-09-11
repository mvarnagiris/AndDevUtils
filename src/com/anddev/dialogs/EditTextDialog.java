package com.anddev.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.anddev.events.DialogEvent;

/**
 * Dialog that shows {@link EditText} as it's custom view.
 * 
 * @author Mantas Varnagiris
 */
public class EditTextDialog extends SimpleDialog
{
	private EditText	edit_ET;
	private String		text;

	// SimpleDialog
	// --------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		// Fix EditText if necessary
		if (edit_ET == null)
		{
			edit_ET = new EditText(getActivity());
			customView = edit_ET;
		}
		edit_ET.setText(text);
		edit_ET.setSelection(text.length());

		// Create dialog
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
					eventBus.post(new EditTextDialogEvent(requestCode, Dialog.BUTTON_POSITIVE, edit_ET.getText().toString()));
				}
			});

		if (!TextUtils.isEmpty(neutralButton))
			builder.setNeutralButton(neutralButton, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					eventBus.post(new EditTextDialogEvent(requestCode, Dialog.BUTTON_NEUTRAL, edit_ET.getText().toString()));
				}
			});

		if (!TextUtils.isEmpty(negativeButton))
			builder.setNegativeButton(negativeButton, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					eventBus.post(new EditTextDialogEvent(requestCode, Dialog.BUTTON_NEGATIVE, edit_ET.getText().toString()));
				}
			});

		if (customView != null)
			builder.setView(customView);

		return builder.create();
	}

	/**
	 * This method does nothing. Use {@link EditTextDialog#setEditTextView(View, int)} instead.
	 */
	@Override
	public void setCustomView(View customView)
	{
		// Do nothing.
	}

	// Public methods
	// --------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Set custom view for dialog.
	 * 
	 * @param editTextView
	 *            If {@code null} then simple {@link EditText} will be created. Can be instance of {@link EditText}, then {@code editTextId} will be ignored.
	 * @param editTextId
	 *            Id for {@link EditText} in given view. Will be ignored if {@code editTextView} is instance of {@link EditText}.
	 */
	public void setEditTextView(View editTextView, int editTextId)
	{
		if (editTextView != null && editTextView instanceof EditText)
		{
			edit_ET = (EditText) editTextView;
			customView = edit_ET;
		}
		else if (editTextView != null && editTextId > 0)
		{
			edit_ET = (EditText) editTextView.findViewById(editTextId);
			customView = editTextView;
		}
	}

	/**
	 * Set default text for {@link EditText}.
	 * 
	 * @param text
	 *            Default text.
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	// Builder
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class Builder
	{
		protected final int	requestCode;
		protected String	title			= null;
		protected String	message			= null;
		protected String	positiveButton	= null;
		protected String	neutralButton	= null;
		protected String	negativeButton	= null;
		protected View		customView		= null;
		protected String	text			= null;
		protected int		editTextId		= 0;

		public Builder(int requestCode)
		{
			this.requestCode = requestCode;
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------------

		/**
		 * Creates new {@link EditTextDialog} with arguments provided for this builder.
		 * 
		 * @return
		 */
		public EditTextDialog create()
		{
			EditTextDialog f = new EditTextDialog();

			f.setRequestCode(requestCode);
			f.setTitle(title);
			f.setMessage(message);
			f.setPositiveButton(positiveButton);
			f.setNeutralButton(neutralButton);
			f.setNegativeButton(negativeButton);
			f.setEditTextView(customView, editTextId);
			f.setText(text);

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
		public Builder setEditTextView(View editTextView, int editTextId)
		{
			this.customView = editTextView;
			this.editTextId = editTextId;
			return this;
		}

		/**
		 * Set default text argument for this builder.
		 * 
		 * @param text
		 *            Default text.
		 * @return Same builder for chaining.
		 */
		public Builder setText(String text)
		{
			this.text = text;
			return this;
		}
	}

	// Events
	// --------------------------------------------------------------------------------------------------------------------------------

	public static class EditTextDialogEvent extends DialogEvent
	{
		public final String	text;

		public EditTextDialogEvent(int requestCode, int buttonId, String text)
		{
			super(requestCode, buttonId);
			this.text = text;
		}

		// Public methods
		// --------------------------------------------------------------------------------------------------------------------------------

		public String getText()
		{
			return text;
		}
	}
}