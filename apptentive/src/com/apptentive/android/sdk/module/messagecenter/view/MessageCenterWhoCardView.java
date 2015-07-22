/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view;

import android.content.Context;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;


import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.Configuration;
import com.apptentive.android.sdk.util.Constants;


/**
 * @author Barry Li
 */
public class MessageCenterWhoCardView extends FrameLayout implements MessageCenterListItemView {

	private EditText etEmail;
	private EditText etName;

	public MessageCenterWhoCardView(Context context, final MessageAdapter.OnComposingActionListener listener) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		View parentView = inflater.inflate(R.layout.apptentive_message_center_who_card, this);
		etEmail = (EditText) parentView.findViewById(R.id.who_email);
		etName = (EditText) parentView.findViewById(R.id.who_name);

		etEmail.addTextChangedListener(new TextWatcher() {
			private boolean doScroll = false;

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				etEmail.setTextColor(getResources().getColor(R.color.apptentive_text_message_text));
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		View skipButton = findViewById(R.id.btn_skip);
		final boolean required = Configuration.load(getContext()).isMessageCenterEmailRequired();
		if (skipButton != null) {
			if (required) {
				skipButton.setVisibility(INVISIBLE);
			} else {
				skipButton.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						listener.onCloseWhoCard();
					}
				});
			}
		}

		View sendButton = findViewById(R.id.btn_send);
		if (required) {
			((Button)sendButton).setText(getResources().getText(R.string.apptentive_send));
		}
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (required) {
					String email = etEmail.getText().toString();
					if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
						etEmail.setTextColor(getResources().getColor(R.color.apptentive_red));
						return;
					}
				}
				SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_EMAIL, etEmail.getText().toString());
				editor.putString(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_NAME, etName.getText().toString());
				editor.commit();
				listener.onCloseWhoCard();
			}
		});

	}

	public EditText getNameField() {
		return etName;
	}

	public EditText getEmailField() {
		return etEmail;
	}
}