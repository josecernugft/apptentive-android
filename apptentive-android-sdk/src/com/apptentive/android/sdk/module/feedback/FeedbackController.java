/*
 * FeedbackControllerNew.java
 *
 * Created by Sky Kelsey on 2011-11-04.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.module.feedback;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.model.ApptentiveModel;
import com.apptentive.android.sdk.model.GlobalInfo;
import com.apptentive.android.sdk.module.ViewController;
import com.apptentive.android.sdk.module.metric.MetricPayload;
import com.apptentive.android.sdk.offline.FeedbackPayload;
import com.apptentive.android.sdk.offline.PayloadManager;

import java.util.Map;

public class FeedbackController implements ViewController {

	private Dialog dialog;
	private Activity activity;
	private FeedbackPayload feedback;

	public enum Trigger{
		rating,
		forced
	}

	public FeedbackController(Activity activity, Trigger reason) {
		this.activity = activity;
		this.feedback = new FeedbackPayload("feedback");
		dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.apptentive_feedback);
		dialog.findViewById(R.id.apptentive_button_cancel).setOnClickListener(clickListener);
		dialog.findViewById(R.id.apptentive_button_send).setOnClickListener(clickListener);

		EditText feedback = (EditText) dialog.findViewById(R.id.apptentive_feedback_text);
		feedback.addTextChangedListener(new GenericTextWatcher(feedback));
		switch(reason){
			case forced:
				feedback.setHint(R.string.apptentive_edittext_feedback_message_forced);
				break;
			case rating:
				feedback.setHint(R.string.apptentive_edittext_feedback_message);
				break;
			default:
				break;
		}
		EditText email = (EditText) dialog.findViewById(R.id.apptentive_feedback_user_email);
		this.feedback.setEmail(GlobalInfo.userEmail);
		email.setText(GlobalInfo.userEmail);
		email.addTextChangedListener(new GenericTextWatcher(email));

		dialog.findViewById(R.id.apptentive_branding_view).setOnClickListener(clickListener);

		// Instrumentation
		MetricPayload metric = new MetricPayload(MetricPayload.Event.feedback_dialog__launch);
		metric.putData("trigger", reason.name());
		PayloadManager.getInstance().putPayload(metric);

		dialog.show();
	}

	public void cleanup() {
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View view) {
			int id = view.getId();
			if(id == R.id.apptentive_button_cancel){
				// Instrumentation
				MetricPayload metric = new MetricPayload(MetricPayload.Event.feedback_dialog__cancel);
				PayloadManager.getInstance().putPayload(metric);
				dialog.dismiss();
			}else if(id == R.id.apptentive_button_send){
				submit();
				dialog.dismiss();
			}else if(id == R.id.apptentive_branding_view){
				Apptentive.getInstance().about(activity);
			}
		}
	};

	private class GenericTextWatcher implements TextWatcher {

		private View view;

		private GenericTextWatcher(View view) {
			this.view = view;
		}

		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		public void afterTextChanged(Editable editable) {
			String text = editable.toString();
			int id = view.getId();
			if(id == R.id.apptentive_feedback_user_email){
				feedback.setEmail(text);
			}else if(id == R.id.apptentive_feedback_text){
				feedback.setFeedback(text);
			}
		}
	}

	private void submit() {

		// Add in the key.value pairs that the developer passed in as "record[data][KEY] = VALUE"
		Map<String, String> pairs = ApptentiveModel.getInstance().getCustomDataFields();
		if(pairs != null){
			for(String key : pairs.keySet()){
				try{
					feedback.setString(pairs.get(key), "record", "data", key);
				}catch(Exception e){
					Log.e("Error setting developer defined custom feedback field", e);
				}
			}
		}
		PayloadManager.getInstance().putPayload(feedback);
	}
}
