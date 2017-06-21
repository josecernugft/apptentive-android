/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.model.CommerceExtendedData;
import com.apptentive.android.sdk.model.ExtendedData;
import com.apptentive.android.sdk.model.LocationExtendedData;
import com.apptentive.android.sdk.model.StoredFile;
import com.apptentive.android.sdk.model.TimeExtendedData;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.apptentive.android.sdk.model.CompoundMessage;
import com.apptentive.android.sdk.module.metric.MetricModule;
import com.apptentive.android.sdk.module.rating.IRatingProvider;
import com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class contains the complete public API for accessing Apptentive features from within your app.
 */
public class Apptentive {

	/**
	 * Must be called from the {@link Application#onCreate()} method in the {@link Application} object defined in your app's manifest.
	 *
	 * @param application The {@link Application} object for this app.
	 */
	public static void register(Application application) {
		Apptentive.register(application, null, null);
	}

	public static void register(Application application, String apptentiveKey, String apptentiveSignature) {
		register(application, apptentiveKey, apptentiveSignature, null);
	}

	private static void register(Application application, String apptentiveKey, String apptentiveSignature, String serverUrl) {
		ApptentiveLog.i("Registering Apptentive.");
		ApptentiveInternal.createInstance(application, apptentiveKey, apptentiveSignature, serverUrl);
	}

	//region Global Data Methods

	/**
	 * Sets the user's email address. This email address will be sent to the Apptentive server to allow out of app
	 * communication, and to help provide more context about this user. This email will be the definitive email address
	 * for this user, unless one is provided directly by the user through an Apptentive UI. Calls to this method are
	 * idempotent. Calls to this method will overwrite any previously entered email, so if you don't want to overwrite
	 * the email provided by the user, make sure to check the value with {@link #getPersonEmail()} before you call this method.
	 *
	 * @param email The user's email address.
	 */
	public static void setPersonEmail(String email) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				// FIXME: Make sure Person object diff is sent.
				conversation.getPerson().setEmail(email);
			}
		}
	}

	/**
	 * Retrieves the user's email address. This address may be set via {@link #setPersonEmail(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's email if set, else null.
	 */
	public static String getPersonEmail() {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				return conversation.getPerson().getEmail();
			}
		}
		return null;
	}

	/**
	 * Sets the user's name. This name will be sent to the Apptentive server and displayed in conversations you have
	 * with this person. This name will be the definitive username for this user, unless one is provided directly by the
	 * user through an Apptentive UI. Calls to this method are idempotent. Calls to this method will overwrite any
	 * previously entered email, so if you don't want to overwrite the email provided by the user, make sure to check
	 * the value with {@link #getPersonName()} before you call this method.
	 *
	 * @param name The user's name.
	 */
	public static void setPersonName(String name) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				// FIXME: Make sure Person object diff is sent.
				conversation.getPerson().setName(name);
			}
		}
	}

	/**
	 * Retrieves the user's name. This name may be set via {@link #setPersonName(String)},
	 * or by the user through Message Center.
	 *
	 * @return The person's name if set, else null.
	 */
	public static String getPersonName() {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				return conversation.getPerson().getName();
			}
		}
		return null;
	}

	/**
	 * Add a custom data String to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A String value.
	 */
	public static void addCustomDeviceData(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			if (value != null) {
				value = value.trim();
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().put(key, value);
			}
		}
	}

	/**
	 * Add a custom data Number to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Number value.
	 */
	public static void addCustomDeviceData(String key, Number value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().put(key, value);
			}
		}
	}

	/**
	 * Add a custom data Boolean to the Device. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Boolean value.
	 */
	public static void addCustomDeviceData(String key, Boolean value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().put(key, value);
			}
		}
	}

	private static void addCustomDeviceData(String key, Version version) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().put(key, version);
			}
		}
	}

	private static void addCustomDeviceData(String key, DateTime dateTime) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().put(key, dateTime);
			}
		}
	}

	/**
	 * Remove a piece of custom data from the device. Calls to this method are idempotent.
	 *
	 * @param key The key to remove.
	 */
	public static void removeCustomDeviceData(String key) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getDevice().getCustomData().remove(key);
			}
		}
	}

	/**
	 * Add a custom data String to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A String value.
	 */
	public static void addCustomPersonData(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			if (value != null) {
				value = value.trim();
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().put(key, value);
			}
		}
	}

	/**
	 * Add a custom data Number to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Number value.
	 */
	public static void addCustomPersonData(String key, Number value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().put(key, value);
			}
		}
	}

	/**
	 * Add a custom data Boolean to the Person. Custom data will be sent to the server, is displayed
	 * in the Conversation view, and can be used in Interaction targeting.  Calls to this method are
	 * idempotent.
	 *
	 * @param key   The key to store the data under.
	 * @param value A Boolean value.
	 */
	public static void addCustomPersonData(String key, Boolean value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().put(key, value);
			}
		}
	}

	private static void addCustomPersonData(String key, Version version) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().put(key, version);
			}
		}
	}

	private static void addCustomPersonData(String key, DateTime dateTime) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().remove(key);
			}
		}
	}

	/**
	 * Remove a piece of custom data from the Person. Calls to this method are idempotent.
	 *
	 * @param key The key to remove.
	 */
	public static void removeCustomPersonData(String key) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getPerson().getCustomData().remove(key);
			}
		}
	}

	//endregion

	//region Third Party Integrations

	/**
	 * For internal use only.
	 */
	public static final String INTEGRATION_PUSH_TOKEN = "token";

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device without a third party push provider. Requires a valid GCM configuration.
	 */
	public static final int PUSH_PROVIDER_APPTENTIVE = 0;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Parse Push integration. Requires a valid Parse integration.
	 */
	public static final int PUSH_PROVIDER_PARSE = 1;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Urban Airship Push integration. Requires a valid Urban
	 * Airship Push integration.
	 */
	public static final int PUSH_PROVIDER_URBAN_AIRSHIP = 2;

	/**
	 * Call {@link #setPushNotificationIntegration(int, String)} with this value to allow Apptentive to send pushes
	 * to this device through your existing Amazon AWS SNS integration. Requires a valid Amazon AWS SNS
	 * integration.
	 */
	public static final int PUSH_PROVIDER_AMAZON_AWS_SNS = 3;

	/**
	 * Sends push provider information to our server to allow us to send pushes to this device when
	 * you reply to your customers. Only one push provider is allowed to be active at a time, so you
	 * should only call this method once. Please see our
	 * <a href="http://www.apptentive.com/docs/android/integration/#push-notifications">integration guide</a> for
	 * instructions.
	 *
	 * @param pushProvider One of the following:
	 *                     <ul>
	 *                     <li>{@link #PUSH_PROVIDER_APPTENTIVE}</li>
	 *                     <li>{@link #PUSH_PROVIDER_PARSE}</li>
	 *                     <li>{@link #PUSH_PROVIDER_URBAN_AIRSHIP}</li>
	 *                     <li>{@link #PUSH_PROVIDER_AMAZON_AWS_SNS}</li>
	 *                     </ul>
	 * @param token        The push provider token you receive from your push provider. The format is push provider specific.
	 *                     <dl>
	 *                     <dt>Apptentive</dt>
	 *                     <dd>If you are using Apptentive to send pushes directly to GCM or FCM, pass in the GCM/FCM Registration ID, which you can
	 *                     <a href="https://github.com/googlesamples/google-services/blob/73f8a4fcfc93da08a40b96df3537bb9b6ef1b0fa/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart/RegistrationIntentService.java#L51">access like this</a>.
	 *                     </dd>
	 *                     <dt>Parse</dt>
	 *                     <dd>The Parse <a href="https://parse.com/docs/android/guide#push-notifications">deviceToken</a></dd>
	 *                     <dt>Urban Airship</dt>
	 *                     <dd>The Urban Airship Channel ID, which you can
	 *                     <a href="https://github.com/urbanairship/android-samples/blob/8ad77e5e81a1b0507c6a2c45a5c30a1e2da851e9/PushSample/src/com/urbanairship/push/sample/IntentReceiver.java#L43">access like this</a>.
	 *                     </dd>
	 *                     <dt>Amazon AWS SNS</dt>
	 *                     <dd>The GCM Registration ID, which you can <a href="http://docs.aws.amazon.com/sns/latest/dg/mobile-push-gcm.html#registration-id-gcm">access like this</a>.</dd>
	 *                     </dl>
	 */
	public static void setPushNotificationIntegration(int pushProvider, String token) {
		if (!ApptentiveInternal.isApptentiveRegistered()) {
			return;
		}
		// Store the push stuff globally
		SharedPreferences prefs = ApptentiveInternal.getInstance().getGlobalSharedPrefs();
		prefs.edit().putInt(Constants.PREF_KEY_PUSH_PROVIDER, pushProvider)
			.putString(Constants.PREF_KEY_PUSH_TOKEN, token)
			.apply();

		// Also set it on the active Conversation, if there is one.
		Conversation conversation = ApptentiveInternal.getInstance().getConversation();
		if (conversation != null) {
			conversation.setPushIntegration(pushProvider, token);
		}
	}

	//endregion

	//region Push Notifications

	/**
	 * Determines whether this Intent is a push notification sent from Apptentive.
	 *
	 * @param intent The received {@link Intent} you received in your BroadcastReceiver.
	 * @return True if the Intent came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return false;
		}
		return ApptentiveInternal.getApptentivePushNotificationData(intent) != null;
	}

	/**
	 * Determines whether this Bundle came from an Apptentive push notification. This method is used with Urban Airship
	 * integrations.
	 *
	 * @param bundle The push payload bundle passed to GCM onMessageReceived() callback
	 * @return True if the push came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Bundle bundle) {
		if (!ApptentiveInternal.checkRegistered()) {
			return false;
		}
		return ApptentiveInternal.getApptentivePushNotificationData(bundle) != null;
	}

	/**
	 * Determines whether push payload data came from an Apptentive push notification.
	 *
	 * @param data The push payload data obtained through FCM's RemoteMessage.getData(), when using FCM
	 * @return True if the push came from, and should be handled by Apptentive.
	 */
	public static boolean isApptentivePushNotification(Map<String, String> data) {
		if (!ApptentiveInternal.checkRegistered()) {
			return false;
		}
		return ApptentiveInternal.getApptentivePushNotificationData(data) != null;
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link android.support.v4.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Intent} that did
	 * not come from Apptentive will return a null object. If you receive a null object, your app will
	 * need to handle this notification itself.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>GCM</li>
	 * <li>AWS SNS</li>
	 * <li>Parse</li>
	 * </ul>
	 *
	 * @param intent An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *               in the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a valid {@link PendingIntent} to launch an Apptentive Interaction if the push data came from Apptentive, or null.
	 */
	public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(intent);
		return ApptentiveInternal.generatePendingIntentFromApptentivePushData(apptentivePushData);
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link android.support.v4.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Bundle} that
	 * did not come from Apptentive will return a null object. If you receive a null object, your app
	 * will need to handle this notification itself.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>Urban Airship</li>
	 * </ul>
	 *
	 * @param bundle A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *               the the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a valid {@link PendingIntent} to launch an Apptentive Interaction if the push data came from Apptentive, or null.
	 */
	public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Bundle bundle) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(bundle);
		return ApptentiveInternal.generatePendingIntentFromApptentivePushData(apptentivePushData);
	}

	/**
	 * <p>Use this method in your push receiver to build a pending Intent when an Apptentive push
	 * notification is received. Pass the generated PendingIntent to
	 * {@link android.support.v4.app.NotificationCompat.Builder#setContentIntent} to allow Apptentive
	 * to display Interactions such as Message Center. Calling this method for a push {@link Bundle} that
	 * did not come from Apptentive will return a null object. If you receive a null object, your app
	 * will need to handle this notification itself.</p>
	 * <p>This is the method you will likely need if you integrated using:</p>
	 * <ul>
	 * <li>Firebase Cloud Messaging (FCM)</li>
	 * </ul>
	 *
	 * @param data A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive Push
	 *             data. Pass in what you receive in the the Service or BroadcastReceiver that is
	 *             used by your chosen push provider.
	 * @return a valid {@link PendingIntent} to launch an Apptentive Interaction if the push data came from Apptentive, or null.
	 */
	public static PendingIntent buildPendingIntentFromPushNotification(@NonNull Map<String, String> data) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		String apptentivePushData = ApptentiveInternal.getApptentivePushNotificationData(data);
		return ApptentiveInternal.generatePendingIntentFromApptentivePushData(apptentivePushData);
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param intent An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *               in the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (intent != null) {
			return getTitleFromApptentivePush(intent.getExtras());
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param intent An {@link Intent} containing the Apptentive Push data. Pass in what you receive
	 *               in the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Intent intent) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (intent != null) {
			return getBodyFromApptentivePush(intent.getExtras());
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param bundle A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *               the the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Bundle bundle) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (bundle == null) {
			return null;
		}
		if (bundle.containsKey(ApptentiveInternal.TITLE_DEFAULT)) {
			return bundle.getString(ApptentiveInternal.TITLE_DEFAULT);
		}
		if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE)) {
			String parseDataString = bundle.getString(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE);
			if (parseDataString != null) {
				try {
					JSONObject parseJson = new JSONObject(parseDataString);
					return parseJson.optString(ApptentiveInternal.TITLE_DEFAULT, null);
				} catch (JSONException e) {
					return null;
				}
			}
		} else if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_UA)) {
			Bundle uaPushBundle = bundle.getBundle(ApptentiveInternal.PUSH_EXTRA_KEY_UA);
			if (uaPushBundle == null) {
				return null;
			}
			return uaPushBundle.getString(ApptentiveInternal.TITLE_DEFAULT);
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param bundle A {@link Bundle} containing the Apptentive Push data. Pass in what you receive in
	 *               the the Service or BroadcastReceiver that is used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Bundle bundle) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (bundle == null) {
			return null;
		}
		if (bundle.containsKey(ApptentiveInternal.BODY_DEFAULT)) {
			return bundle.getString(ApptentiveInternal.BODY_DEFAULT);
		}
		if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE)) {
			String parseDataString = bundle.getString(ApptentiveInternal.PUSH_EXTRA_KEY_PARSE);
			if (parseDataString != null) {
				try {
					JSONObject parseJson = new JSONObject(parseDataString);
					return parseJson.optString(ApptentiveInternal.BODY_PARSE, null);
				} catch (JSONException e) {
					return null;
				}
			}
		} else if (bundle.containsKey(ApptentiveInternal.PUSH_EXTRA_KEY_UA)) {
			Bundle uaPushBundle = bundle.getBundle(ApptentiveInternal.PUSH_EXTRA_KEY_UA);
			if (uaPushBundle == null) {
				return null;
			}
			return uaPushBundle.getString(ApptentiveInternal.BODY_UA);
		} else if (bundle.containsKey(ApptentiveInternal.BODY_UA)) {
			return bundle.getString(ApptentiveInternal.BODY_UA);
		}
		return null;
	}

	/**
	 * Use this method in your push receiver to get the notification title you can use to construct a
	 * {@link android.app.Notification} object.
	 *
	 * @param data A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive Push
	 *             data. Pass in what you receive in the the Service or BroadcastReceiver that is
	 *             used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getTitleFromApptentivePush(Map<String, String> data) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (data == null) {
			return null;
		}
		return data.get(ApptentiveInternal.TITLE_DEFAULT);
	}

	/**
	 * Use this method in your push receiver to get the notification body text you can use to
	 * construct a {@link android.app.Notification} object.
	 *
	 * @param data A {@link Map}&lt;{@link String},{@link String}&gt; containing the Apptentive Push
	 *             data. Pass in what you receive in the the Service or BroadcastReceiver that is
	 *             used by your chosen push provider.
	 * @return a String value, or null.
	 */
	public static String getBodyFromApptentivePush(Map<String, String> data) {
		if (!ApptentiveInternal.checkRegistered()) {
			return null;
		}
		if (data == null) {
			return null;
		}
		return data.get(ApptentiveInternal.BODY_DEFAULT);
	}

	//endregion

	//region Rating

	/**
	 * Use this to choose where to send the user when they are prompted to rate the app. This should be the same place
	 * that the app was downloaded from.
	 *
	 * @param ratingProvider A {@link IRatingProvider} value.
	 */

	public static void setRatingProvider(IRatingProvider ratingProvider) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().setRatingProvider(ratingProvider);
		}
	}

	/**
	 * If there are any properties that your {@link IRatingProvider} implementation requires, populate them here. This
	 * is not currently needed with the Google Play and Amazon Appstore IRatingProviders.
	 *
	 * @param key   A String
	 * @param value A String
	 */
	public static void putRatingProviderArg(String key, String value) {
		if (ApptentiveInternal.isApptentiveRegistered()) {
			ApptentiveInternal.getInstance().putRatingProviderArg(key, value);
		}
	}

	//endregion

	//region Message Center

	/**
	 * Opens the Apptentive Message Center UI Activity
	 *
	 * @param context The context from which to launch the Message Center
	 * @return true if Message Center was shown, else false.
	 */
	public static boolean showMessageCenter(Context context) {
		return showMessageCenter(context, null);
	}

	/**
	 * Opens the Apptentive Message Center UI Activity, and allows custom data to be sent with the next message the user
	 * sends. If the user sends multiple messages, this data will only be sent with the first message sent after this
	 * method is invoked. Additional invocations of this method with custom data will repeat this process.
	 *
	 * @param context    The context from which to launch the Message Center. This should be an
	 *                   Activity, except in rare cases where you don't have access to one, in which
	 *                   case Apptentive Message Center will launch in a new task.
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans.
	 *                   If any message is sent by the Person, this data is sent with it, and then
	 *                   cleared. If no message is sent, this data is discarded.
	 * @return true if Message Center was shown, else false.
	 */
	public static boolean showMessageCenter(Context context, Map<String, Object> customData) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.v(ApptentiveLogTag.MESSAGES, "No active Conversation.");
				return false;
			}
			return ApptentiveInternal.getInstance().showMessageCenterInternal(context, customData);
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.showMessageCenter()", e);
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	/**
	 * Our SDK must connect to our server at least once to download initial configuration for Message
	 * Center. Call this method to see whether or not Message Center can be displayed.
	 *
	 * @return true if a call to {@link #showMessageCenter(Context)} will display Message Center, else false.
	 */
	public static boolean canShowMessageCenter() {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.v(ApptentiveLogTag.MESSAGES, "No active Conversation.");
				return false;
			}
			return ApptentiveInternal.getInstance().canShowMessageCenterInternal();
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.canShowMessageCenter()", e);
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	/**
	 * Set one and only listener to be notified when the number of unread messages in the Message Center changes.
	 * if the app calls this method to set up a custom listener, the apptentive unread message badge, also an UnreadMessagesListener,
	 * won't get notification. Please use {@link #addUnreadMessagesListener(UnreadMessagesListener)} instead.
	 *
	 * @param listener An UnreadMessagesListener that you instantiate. Pass null to remove existing listener.
	 *                 Do not pass in an anonymous class, such as setUnreadMessagesListener(new UnreadMessagesListener() {...}).
	 *                 Instead, create your listener as an instance variable and pass that in. This
	 *                 allows us to keep a weak reference to avoid memory leaks.
	 */
	@Deprecated
	public static void setUnreadMessagesListener(UnreadMessagesListener listener) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.v(ApptentiveLogTag.MESSAGES, "No active Conversation.");
				return;
			}
			ApptentiveInternal.getInstance().getMessageManager().setHostUnreadMessagesListener(listener);
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.setUnreadMessagesListener()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Add a listener to be notified when the number of unread messages in the Message Center changes.
	 *
	 * @param listener An UnreadMessagesListener that you instantiate. Do not pass in an anonymous class.
	 *                 Instead, create your listener as an instance variable and pass that in. This
	 *                 allows us to keep a weak reference to avoid memory leaks.
	 */
	public static void addUnreadMessagesListener(UnreadMessagesListener listener) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.v(ApptentiveLogTag.MESSAGES, "No active Conversation.");
				return;
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			if (conversation != null) {
				conversation.getMessageManager().addHostUnreadMessagesListener(listener);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.addUnreadMessagesListener()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Returns the number of unread messages in the Message Center.
	 *
	 * @return The number of unread messages.
	 */
	public static int getUnreadMessageCount() {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.v(ApptentiveLogTag.MESSAGES, "No active Conversation.");
				return 0;
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			return conversation.getMessageManager().getUnreadMessageCount();
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.getUnreadMessageCount()", e);
			MetricModule.sendError(e, null, null);
		}
		return 0;
	}

	/**
	 * Sends a text message to the server. This message will be visible in the conversation view on the server, but will
	 * not be shown in the client's Message Center.
	 *
	 * @param text The message you wish to send.
	 */
	public static void sendAttachmentText(String text) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.i(ApptentiveLogTag.MESSAGES, "Can't send attachment: No active Conversation.");
				return;
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			CompoundMessage message = new CompoundMessage();
			message.setBody(text);
			message.setRead(true);
			message.setHidden(true);
			message.setSenderId(conversation.getPerson().getId());
			message.setAssociatedFiles(null);
			MessageManager mgr = conversation.getMessageManager();
			if (mgr != null) {
				mgr.sendMessage(message);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.sendAttachmentText(String)", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param uri The URI of the local resource file.
	 */
	public static void sendAttachmentFile(String uri) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.i(ApptentiveLogTag.MESSAGES, "Can't send attachment: No active Conversation.");
				return;
			}
			if (TextUtils.isEmpty(uri)) {
				return;
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			CompoundMessage message = new CompoundMessage();
			// No body, just attachment
			message.setBody(null);
			message.setRead(true);
			message.setHidden(true);
			message.setSenderId(conversation.getPerson().getId());

			ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
			/* Make a local copy in the cache dir. By default the file name is "apptentive-api-file + nonce"
			 * If original uri is known, the name will be taken from the original uri
			 */
			Context context = ApptentiveInternal.getInstance().getApplicationContext();
			String localFilePath = Util.generateCacheFilePathFromNonceOrPrefix(context, message.getNonce(), Uri.parse(uri).getLastPathSegment());

			String mimeType = Util.getMimeTypeFromUri(context, Uri.parse(uri));
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String extension = mime.getExtensionFromMimeType(mimeType);

			// If we can't get the mime type from the uri, try getting it from the extension.
			if (extension == null) {
				extension = MimeTypeMap.getFileExtensionFromUrl(uri);
			}
			if (mimeType == null && extension != null) {
				mimeType = mime.getMimeTypeFromExtension(extension);
			}
			if (!TextUtils.isEmpty(extension)) {
				localFilePath += "." + extension;
			}
			StoredFile storedFile = Util.createLocalStoredFile(uri, localFilePath, mimeType);
			if (storedFile == null) {
				return;
			}

			storedFile.setId(message.getNonce());
			attachmentStoredFiles.add(storedFile);

			message.setAssociatedFiles(attachmentStoredFiles);
			MessageManager mgr = ApptentiveInternal.getInstance().getMessageManager();
			if (mgr != null) {
				mgr.sendMessage(message);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.sendAttachmentFile(String)", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param content  A byte array of the file contents.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(byte[] content, String mimeType) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.i(ApptentiveLogTag.MESSAGES, "Can't send attachment: No active Conversation.");
				return;
			}
			ByteArrayInputStream is = null;
			try {
				is = new ByteArrayInputStream(content);
				sendAttachmentFile(is, mimeType);
			} finally {
				Util.ensureClosed(is);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.sendAttachmentFile(byte[], String)", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Sends a file to the server. This file will be visible in the conversation view on the server, but will not be shown
	 * in the client's Message Center. A local copy of this file will be made until the message is transmitted, at which
	 * point the temporary file will be deleted.
	 *
	 * @param is       An InputStream from the desired file.
	 * @param mimeType The mime type of the file.
	 */
	public static void sendAttachmentFile(InputStream is, String mimeType) {
		try {
			if (!ApptentiveInternal.isConversationActive()) {
				ApptentiveLog.i(ApptentiveLogTag.MESSAGES, "Can't send attachment: No active Conversation.");
				return;
			}
			if (is == null) {
				return;
			}
			Conversation conversation = ApptentiveInternal.getInstance().getConversation();
			CompoundMessage message = new CompoundMessage();
			// No body, just attachment
			message.setBody(null);
			message.setRead(true);
			message.setHidden(true);
			message.setSenderId(conversation.getPerson().getId());

			ArrayList<StoredFile> attachmentStoredFiles = new ArrayList<StoredFile>();
			String localFilePath = Util.generateCacheFilePathFromNonceOrPrefix(ApptentiveInternal.getInstance().getApplicationContext(), message.getNonce(), null);

			String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
			if (!TextUtils.isEmpty(extension)) {
				localFilePath += "." + extension;
			}
			// When created from InputStream, there is no source file uri or path, thus just use the cache file path
			StoredFile storedFile = Util.createLocalStoredFile(is, localFilePath, localFilePath, mimeType);
			if (storedFile == null) {
				return;
			}
			storedFile.setId(message.getNonce());
			attachmentStoredFiles.add(storedFile);

			message.setAssociatedFiles(attachmentStoredFiles);
			ApptentiveInternal.getInstance().getMessageManager().sendMessage(message);
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.sendAttachmentFile(InputStream, String)", e);
			MetricModule.sendError(e, null, null);
		}
	}

	//endregion

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, determines
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param context The context from which to launch the Interaction. This should be an
	 *                Activity, except in rare cases where you don't have access to one, in which
	 *                case Apptentive Interactions will launch in a new task.
	 * @param event   A unique String representing the line this method is called on. For instance, you may want to have
	 *                the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                call <strong><code>engage(context, "finished_upload");</code></strong>
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Context context, String event) {
		return EngagementModule.engage(context, "local", "app", null, event, null, null, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, determines
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param context    The context from which to launch the Interaction. This should be an
	 *                   Activity, except in rare cases where you don't have access to one, in which
	 *                   case Apptentive Interactions will launch in a new task.
	 * @param event      A unique String representing the line this method is called on. For instance, you may want to have
	 *                   the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                   call <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param customData A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans. This data
	 *                   is sent to the server for tracking information in the context of the engaged Event.
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Context context, String event, Map<String, Object> customData) {
		return EngagementModule.engage(context, "local", "app", null, event, null, customData, (ExtendedData[]) null);
	}

	/**
	 * This method takes a unique event string, stores a record of that event having been visited, determines
	 * if there is an interaction that is able to run for this event, and then runs it. If more than one interaction
	 * can run, then the most appropriate interaction takes precedence. Only one interaction at most will run per
	 * invocation of this method.
	 *
	 * @param context      The context from which to launch the Interaction. This should be an
	 *                     Activity, except in rare cases where you don't have access to one, in which
	 *                     case Apptentive Interactions will launch in a new task.
	 * @param event        A unique String representing the line this method is called on. For instance, you may want to have
	 *                     the ability to target interactions to run after the user uploads a file in your app. You may then
	 *                     call <strong><code>engage(context, "finished_upload");</code></strong>
	 * @param customData   A Map of String keys to Object values. Objects may be Strings, Numbers, or Booleans. This data
	 *                     is sent to the server for tracking information in the context of the engaged Event.
	 * @param extendedData An array of ExtendedData objects. ExtendedData objects used to send structured data that has
	 *                     specific meaning to the server. By using an {@link ExtendedData} object instead of arbitrary
	 *                     customData, special meaning can be derived. Supported objects include {@link TimeExtendedData},
	 *                     {@link LocationExtendedData}, and {@link CommerceExtendedData}. Include each type only once.
	 * @return true if the an interaction was shown, else false.
	 */
	public static synchronized boolean engage(Context context, String event, Map<String, Object> customData, ExtendedData... extendedData) {
		return EngagementModule.engage(context, "local", "app", null, event, null, customData, extendedData);
	}

	/**
	 * @param event A unique String representing the line this method is called on. For instance, you may want to have
	 *              the ability to target interactions to run after the user uploads a file in your app. You may then
	 *              call <strong><code>engage(context, "finished_upload");</code></strong>
	 * @return true if an immediate call to engage() with the same event name would result in an Interaction being displayed, otherwise false.
	 * @deprecated Use {@link #canShowInteraction(String)}() instead. The behavior is identical. Only the name has changed.
	 */
	public static synchronized boolean willShowInteraction(String event) {
		return canShowInteraction(event);
	}

	/**
	 * This method can be used to determine if a call to one of the <strong><code>engage()</code></strong> methods such as
	 * {@link #engage(Context, String)} using the same event name will
	 * result in the display of an  Interaction. This is useful if you need to know whether an Interaction will be
	 * displayed before you create a UI Button, etc.
	 *
	 * @param event A unique String representing the line this method is called on. For instance, you may want to have
	 *              the ability to target interactions to run after the user uploads a file in your app. You may then
	 *              call <strong><code>engage(activity, "finished_upload");</code></strong>
	 * @return true if an immediate call to engage() with the same event name would result in an Interaction being displayed, otherwise false.
	 */
	public static synchronized boolean canShowInteraction(String event) {
		try {
			if (ApptentiveInternal.isConversationActive()) {
				return EngagementModule.canShowInteraction("local", "app", event);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.canShowInteraction()", e);
			MetricModule.sendError(e, null, null);
		}
		return false;
	}

	/**
	 * Pass in a listener. The listener will be called whenever a survey is finished.
	 * Do not pass in an anonymous class, such as setOnSurveyFinishedListener(new OnSurveyFinishedListener() {...}).
	 * Instead, create your listener as an instance variable and pass that in. This allows us to keep
	 * a weak reference to avoid memory leaks.
	 *
	 * @param listener The {@link com.apptentive.android.sdk.module.survey.OnSurveyFinishedListener} listener
	 *                 to call when the survey is finished.
	 */
	public static void setOnSurveyFinishedListener(OnSurveyFinishedListener listener) {
		try {
			ApptentiveInternal internal = ApptentiveInternal.getInstance();
			if (internal != null) {
				internal.setOnSurveyFinishedListener(listener);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.setOnSurveyFinishedListener()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	//region Login/Logout

	/**
	 * Starts login process asynchronously. This call returns immediately.
	 */
	public static void login(String token, LoginCallback callback) {
		try {
			if (token == null) {
				if (callback != null) {
					callback.onLoginFail("token is null");
				}
				return;
			}

			final ApptentiveInternal instance = ApptentiveInternal.getInstance();
			if (instance == null) {
				ApptentiveLog.e("Unable to login: Apptentive instance is not properly initialized");
				if (callback != null) {
					callback.onLoginFail("Apptentive instance is not properly initialized");
				}
			} else {
				ApptentiveInternal.getInstance().login(token, callback);
			}
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.login()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	/**
	 * Callback interface for an async login process.
	 */
	public interface LoginCallback {
		/**
		 * Called when a login attempt has completed successfully.
		 */
		void onLoginFinish();

		/**
		 * Called when a login attempt has failed.
		 *
		 * @param errorMessage failure cause message
		 */
		void onLoginFail(String errorMessage);
	}

	public static void logout() {
		try {
			final ApptentiveInternal instance = ApptentiveInternal.getInstance();
			if (instance == null) {
				ApptentiveLog.e("Unable to logout: Apptentive instance is not properly initialized");
			} else {
				instance.logout();
			}
		} catch (Exception e) {
			ApptentiveLog.e("Exception in Apptentive.logout()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	public static void addAuthenticationFailureListener(AuthenticationFailedListener listener) {
		try {
			if (!ApptentiveInternal.checkRegistered()) {
				return;
			}
			ApptentiveInternal.getInstance().setAuthenticationFailureListener(listener);
		} catch (Exception e) {
			ApptentiveLog.w("Error in Apptentive.addUnreadMessagesListener()", e);
			MetricModule.sendError(e, null, null);
		}
	}

	public interface AuthenticationFailedListener {
		void onAuthenticationFailed(AuthenticationFailedReason reason);
	}

	public enum AuthenticationFailedReason {
		UNKNOWN,
		INVALID_ALGORITHM,
		MALFORMED_TOKEN,
		INVALID_TOKEN,
		MISSING_SUB_CLAIM,
		MISMATCHED_SUB_CLAIM,
		INVALID_SUB_CLAIM,
		EXPIRED_TOKEN,
		REVOKED_TOKEN,
		MISSING_APP_KEY,
		MISSING_APP_SIGNATURE,
		INVALID_KEY_SIGNATURE_PAIR;

		private String error;

		public String message() {
			return error;
		}

		public static AuthenticationFailedReason parse(String errorType, String error) {
			try {
				AuthenticationFailedReason ret = AuthenticationFailedReason.valueOf(errorType);
				ret.error = error;
				return ret;
			} catch (IllegalArgumentException e) {
				ApptentiveLog.w("Error parsing unknown Apptentive.AuthenticationFailedReason: %s", errorType);
			}
			return UNKNOWN;
		}

		@Override
		public String toString() {
			return "AuthenticationFailedReason{" +
				       "error='" + error + '\'' +
				       "errorType='" + name() + '\'' +
				       '}';
		}
	}

	//endregion

	/**
	 * <p>This type represents a <a href="http://semver.org/">semantic version</a>. It can be initialized
	 * with a string or a long, and there is no limit to the number of parts your semantic version can
	 * contain. The class allows comparison based on semantic version rules.
	 * Valid versions (In sorted order):</p>
	 * <ul>
	 * <li>0</li>
	 * <li>0.1</li>
	 * <li>1.0.0</li>
	 * <li>1.0.9</li>
	 * <li>1.0.10</li>
	 * <li>1.2.3</li>
	 * <li>5</li>
	 * </ul>
	 * Invalid versions:
	 * <ul>
	 * <li>zero</li>
	 * <li>0.1+2015.10.21</li>
	 * <li>1.0.0a</li>
	 * <li>1.0-rc2</li>
	 * <li>1.0.10-SNAPSHOT</li>
	 * <li>5a</li>
	 * <li>FF01</li>
	 * </ul>
	 */
	public static class Version implements Serializable, Comparable<Version> {

		private static final long serialVersionUID = 1L;

		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "version";

		private String version;

		public Version() {
		}

		public Version(JSONObject json) throws JSONException {
			this.version = json.optString(TYPE, null);
		}

		public Version(long version) {
			this.version = Long.toString(version);
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public void setVersion(long version) {
			setVersion(Long.toString(version));
		}

		public String getVersion() {
			return version;
		}

		public void toJsonObject() {
			JSONObject ret = new JSONObject();
			try {
				ret.put(KEY_TYPE, TYPE);
				ret.put(TYPE, version);
			} catch (JSONException e) {
				ApptentiveLog.e("Error creating Apptentive.Version.", e);
			}
		}

		@Override
		public int compareTo(Version other) {
			String thisVersion = getVersion();
			String thatVersion = other.getVersion();
			String[] thisArray = thisVersion.split("\\.");
			String[] thatArray = thatVersion.split("\\.");

			int maxParts = Math.max(thisArray.length, thatArray.length);
			for (int i = 0; i < maxParts; i++) {
				// If one SemVer has more parts than another, pad out the short one with zeros in each slot.
				long left = 0;
				if (thisArray.length > i) {
					left = Long.parseLong(thisArray[i]);
				}
				long right = 0;
				if (thatArray.length > i) {
					right = Long.parseLong(thatArray[i]);
				}
				if (left < right) {
					return -1;
				} else if (left > right) {
					return 1;
				}
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o instanceof Version) {
				return compareTo((Version) o) == 0;
			}
			return false;
		}

		@Override
		public String toString() {
			return getVersion();
		}
	}

	public static class DateTime implements Serializable, Comparable<DateTime> {
		public static final String KEY_TYPE = "_type";
		public static final String TYPE = "datetime";
		public static final String SEC = "sec";

		private String sec;

		public DateTime(JSONObject json) throws JSONException {
			this.sec = json.optString(SEC);
		}

		public DateTime(double dateTime) {
			setDateTime(dateTime);
		}

		public void setDateTime(double dateTime) {
			sec = String.valueOf(dateTime);
		}

		public double getDateTime() {
			return Double.valueOf(sec);
		}

		public JSONObject toJSONObject() {
			JSONObject ret = new JSONObject();
			try {
				ret.put(KEY_TYPE, TYPE);
				ret.put(SEC, sec);
			} catch (JSONException e) {
				ApptentiveLog.e("Error creating Apptentive.DateTime.", e);
			}
			return ret;
		}

		@Override
		public String toString() {
			return Double.toString(getDateTime());
		}

		@Override
		public int compareTo(DateTime other) {
			double thisDateTime = getDateTime();
			double thatDateTime = other.getDateTime();
			return Double.compare(thisDateTime, thatDateTime);
		}
	}
}
