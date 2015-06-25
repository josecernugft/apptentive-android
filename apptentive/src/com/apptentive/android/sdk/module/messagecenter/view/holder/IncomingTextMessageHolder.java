/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter.view.holder;

import android.graphics.Bitmap;
import android.widget.TextView;

import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.messagecenter.view.AvatarView;
import com.apptentive.android.sdk.module.messagecenter.view.IncomingTextMessageView;

/**
 * @author Sky Kelsey
 */
public class IncomingTextMessageHolder extends MessageHolder {
	public AvatarView avatar;
	public TextView text;

	public IncomingTextMessageHolder(IncomingTextMessageView view) {
		super(view);
		avatar = (AvatarView) view.findViewById(R.id.avatar);
		text = (TextView) view.findViewById(R.id.text);
	}

	public void updateMessage(String timestamp, Bitmap avatarBitmap, String text) {
		super.updateMessage(timestamp);
		if (this.avatar != null) {
			this.avatar.setImageBitmap(avatarBitmap);
		}
		if (this.text != null) {
			this.text.setText(text);
		}
	}
}
