/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.view;

import android.os.Bundle;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.ViewActivity;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

/**
 * @author Sky Kelsey
 */
public abstract class InteractionView<T extends Interaction> extends ActivityContent {

	protected T interaction;

	private static final String HAS_LAUNCHED = "has_launched";

	protected boolean hasLaunched;

	public InteractionView(T interaction) {
		this.interaction = interaction;
	}

	public void onCreate(final ViewActivity activity, Bundle savedInstanceState) {
		Log.d("Showing interaction.");
		if (savedInstanceState != null) {
			hasLaunched = savedInstanceState.getBoolean(HAS_LAUNCHED);
		}
		if (!hasLaunched) {
			hasLaunched = true;
			interaction.sendLaunchEvent(activity);
		}
		doOnCreate(activity, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(HAS_LAUNCHED, hasLaunched);
		outState.putString(Interaction.JSON_STRING, interaction.toString());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		hasLaunched = savedInstanceState.getBoolean(HAS_LAUNCHED, false);
	}

	protected abstract void doOnCreate(ViewActivity viewActivity, Bundle savedInstanceState);
}
