package com.tch.lingoine.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.github.bijoysingh.starter.Functions;
import com.github.bijoysingh.starter.server.AccessItem;
import com.github.bijoysingh.starter.util.FileManager;
import com.tch.lingoine.R;
import com.tch.lingoine.items.LanguageItem;
import com.tch.lingoine.server.Access;
import com.tch.lingoine.server.AccessIds;
import com.tch.lingoine.server.Filenames;
import com.tch.lingoine.server.Links;
import com.tch.lingoine.utils.UIUtils;
import com.tch.lingoine.views.LanguageItemView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends ActivityBase {

    LinearLayout learnLeft, learnRight;
    LinearLayout helpLeft, helpRight;
    public static IKandyIncomingCall mIncomingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupLayouts();

        preferences.firstTimeLoggedIn();

        requestData();
        refreshView();
        setCandyChangeListener();
        setupChatListener();
        setListeners();
        setToolbar();
    }

    public void setupLayouts() {
        helpLeft = (LinearLayout) findViewById(R.id.help_left);
        helpRight = (LinearLayout) findViewById(R.id.help_right);
        learnLeft = (LinearLayout) findViewById(R.id.learn_left);
        learnRight = (LinearLayout) findViewById(R.id.learn_right);
    }

    public void requestData() {
        Access access = new Access(context);
        access.get(new AccessItem(Links.getKnownLanguages(), Filenames.getKnownLanguages(),
            AccessIds.GET_LANGUAGE_KNOWN, true).setActivity(this));
        access.get(new AccessItem(Links.getLearningLanguages(), Filenames.getLearningLanguages(),
            AccessIds.GET_LANGUAGE_LEARNING, true).setActivity(this));
    }

    public List<LanguageItem> getLanguageList(String filename) {
        List<LanguageItem> languageList = new ArrayList<>();
        try {
            String text = FileManager.read(context, filename);
            JSONObject json = new JSONObject(text);
            JSONArray array = json.getJSONArray("results");
            for (int position = 0; position < array.length(); position++) {
                JSONObject jsonItem = array.getJSONObject(position);
                LanguageItem item = new LanguageItem(jsonItem);
                languageList.add(item);
            }
        } catch (Exception e) {
            Log.e("LANGUAGE_CHOOSER", e.getMessage(), e);
        }
        return languageList;
    }

    public void refreshView() {
        View learnMore = findViewById(R.id.learn_more);
        View supportMore = findViewById(R.id.support_more);
        View learnHeading = findViewById(R.id.learn_heading);
        View supportHeading = findViewById(R.id.support_heading);

        helpLeft.removeAllViews();
        helpRight.removeAllViews();
        learnLeft.removeAllViews();
        learnRight.removeAllViews();

        List<LanguageItem> knownLanguageList = getLanguageList(Filenames.getKnownLanguages());
        List<LanguageItem> learningLanguageList = getLanguageList(Filenames.getLearningLanguages());

        if (!knownLanguageList.isEmpty()) {
            supportMore.setVisibility(View.GONE);
            Integer position = 0;
            for (LanguageItem item : knownLanguageList) {
                final LanguageItem tItem = item;
                LanguageItemView view = new LanguageItemView(context);
                view.setLanguage(item);
                view.video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestCall(tItem.id, AccessIds.GET_LANGUAGE_USER);
                    }
                });
                view.message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestCall(tItem.id, AccessIds.GET_LANGUAGE_USER_CHAT);
                    }
                });

                if (position % 2 == 0) {
                    helpLeft.addView(view);
                } else {
                    helpRight.addView(view);
                }
                position++;
            }
        } else {
            supportMore.setVisibility(View.VISIBLE);
        }

        if (learningLanguageList.size() > 0) {
            learnMore.setVisibility(View.GONE);
            Integer position = 0;
            for (LanguageItem item : learningLanguageList) {
                final LanguageItem tItem = item;
                LanguageItemView view = new LanguageItemView(context);
                view.setLanguage(item);
                view.video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestCall(tItem.id, AccessIds.GET_LANGUAGE_USER);
                    }
                });
                view.message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestCall(tItem.id, AccessIds.GET_LANGUAGE_USER_CHAT);
                    }
                });

                if (position % 2 == 0) {
                    learnLeft.addView(view);
                } else {
                    learnRight.addView(view);
                }
                position++;
            }
        } else {
            learnMore.setVisibility(View.VISIBLE);
        }

        learnHeading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LanguageChooser.class);
                intent.putExtra(LanguageChooser.CHOOSE_TYPE, LanguageChooser.LEARN_LANGUAGES);
                startActivity(intent);
                finish();
            }
        });


        supportHeading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LanguageChooser.class);
                intent.putExtra(LanguageChooser.CHOOSE_TYPE, LanguageChooser.KNOWN_LANGUAGES);
                startActivity(intent);
                finish();
            }
        });

    }

    public void setCandyChangeListener() {
        if (!Kandy.getAccess().getConnectionState().equals(KandyConnectionState.CONNECTED)) {
            Functions.makeToast(context, "Logging You Into KANDY");
            login();
        }
        Kandy.getAccess().registerNotificationListener(new KandyConnectServiceNotificationListener() {

            @Override
            public void onSocketFailedWithError(String error) {
                //TODO insert your code here
                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Socket Failed")
                    .setContentText("Socket failed with error")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onSocketDisconnected() {
                //TODO insert your code here
                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Disconnected")
                    .setContentText("Socket got disconnected")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onSocketConnecting() {
                //TODO insert your code here
                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Connecting")
                    .setContentText("Socket is connecting")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onSocketConnected() {
                //TODO insert your code here
                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Connected")
                    .setContentText("Socket is connected")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onConnectionStateChanged(KandyConnectionState state) {
                //TODO insert your code here

            }

            @Override
            public void onInvalidUser(String error) {
                //TODO insert your code here

                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Invalid User")
                    .setContentText("User is invalid")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onSessionExpired(String error) {
                //TODO insert your code here

                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Kandy Session Expired")
                    .setContentText("Socket is expired")
                    .setSmallIcon(R.drawable.chat)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(2, notification);
            }

            @Override
            public void onSDKNotSupported(String error) {
                //TODO insert your code here
            }

            @Override
            public void onCertificateError(String s) {

            }

            @Override
            public void onServerConfigurationReceived(JSONObject jsonObject) {

            }
        });
    }

    public void login() {
        KandyRecord kandyRecord;
        try {
            kandyRecord = new KandyRecord(preferences.getCompleteUsername());
        } catch (KandyIllegalArgumentException e) {
            Functions.makeToast(context, "Something went wrong, check username");
            return;
        }
        Kandy.getAccess().login(kandyRecord, preferences.getPassword(), new KandyLoginResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Functions.makeToast(context, err);
            }

            @Override
            public void onLoginSucceeded() {

            }
        });
    }

    public void setListeners() {
        Kandy.getServices().getCallService().registerNotificationListener(new KandyCallServiceNotificationListener() {

            @Override
            public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
            }

            @Override
            public void onIncomingCall(IKandyIncomingCall call) {
                PendingIntent intent;
                Intent resultIntent = new Intent(context, VideoActivity.class);
                resultIntent.putExtra(VideoActivity.INCOMING_CALL, true);
                intent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
                mIncomingCall = call;
                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Incoming Call")
                    .setContentText("You have an incoming call.")
                    .setSmallIcon(R.drawable.video_cam)
                    .setContentIntent(intent)
                    .build();

                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(3, notification);
            }

            @Override
            public void onMissedCall(KandyMissedCallMessage kandyMissedCallMessage) {
            }

            @Override
            public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
                //TODO insert your code here
            }

            @Override
            public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
                //TODO insert your code here
            }

            @Override
            public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
                //TODO insert your code here
            }

            @Override
            public void onCallStateChanged(KandyCallState state, IKandyCall call) {
                PendingIntent intent;
                if (call.getCallState().equals(KandyCallState.RINGING)) {
                    return;
                }
                Intent resultIntent = new Intent(context, HomeActivity.class);
                intent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );

                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Call State Changed")
                    .setContentText(call.getVia() + " : " + state.name() + " : " + call.getCallee())
                    .setSmallIcon(R.drawable.video_cam)
                    .setContentIntent(intent)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(3, notification);
            }

            @Override
            public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage kandyWaitingVoiceMailMessage) {

            }
        });
    }

    ProgressDialog dialog;

    public void requestCall(Integer language, Integer type) {
        dialog = ProgressDialog.show(context, "Finding User",
            "Please wait, while we find a match", true);
        dialog.setCancelable(true);
        Access access = new Access(context);
        access.get(new AccessItem(Links.getUserForLanguage(language), null, type, true)
            .setActivity(this));
    }

    public void handleCallResponse(String response, Integer type) {
        try {
            JSONObject json = new JSONObject(response);
            String email = json.getString("email");
            Functions.makeToast(context, email);
            if (email.contains("@")) {
                String phoneNumber = email.substring(0, email.indexOf("@"));
                Functions.makeToast(context, phoneNumber);

                if (type.equals(AccessIds.GET_LANGUAGE_USER)) {
                    Intent intent = new Intent(context, VideoActivity.class);
                    intent.putExtra(VideoActivity.INCOMING_CALL, false);
                    intent.putExtra(VideoActivity.CALL_NUMBER, phoneNumber);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ChatServiceActivity.class);
                    intent.putExtra(VideoActivity.INCOMING_CALL, false);
                    intent.putExtra(VideoActivity.CALL_NUMBER, phoneNumber);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            Functions.makeToast(context, "Something went wrong");
        }
        if (dialog != null)
            dialog.cancel();
    }

    public void setupChatListener() {
        Kandy.getServices().getChatService().registerNotificationListener(new KandyChatServiceNotificationListener() {
            @Override
            public void onChatReceived(IKandyMessage message, KandyRecordType recordType) {
                PendingIntent intent;
                Intent resultIntent = new Intent(context, ChatServiceActivity.class);
                resultIntent.putExtra(ChatServiceActivity.INCOMING_CALL, true);
                intent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );

                Notification notification = new Notification.Builder(context)
                    .setContentTitle("Message: " + message.getSender().getUserName())
                    .setContentText(message.getMediaItem().getMessage())
                    .setSmallIcon(R.drawable.chat)
                    .setContentIntent(intent)
                    .build();
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(4, notification);
            }

            @Override
            public void onChatDelivered(KandyDeliveryAck message) {
                // TODO insert your code here
            }

            @Override
            public void onChatMediaAutoDownloadProgress(IKandyMessage iKandyMessage, IKandyTransferProgress iKandyTransferProgress) {

            }

            @Override
            public void onChatMediaAutoDownloadFailed(IKandyMessage message, int errorCode, String error) {
                // TODO insert your code here
            }

            @Override
            public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri path) {
                // TODO insert your code here
            }
        });
    }
}
