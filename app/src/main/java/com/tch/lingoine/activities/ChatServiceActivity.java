package com.tch.lingoine.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.IKandyAudioItem;
import com.genband.kandy.api.services.chats.IKandyContactItem;
import com.genband.kandy.api.services.chats.IKandyFileItem;
import com.genband.kandy.api.services.chats.IKandyImageItem;
import com.genband.kandy.api.services.chats.IKandyLocationItem;
import com.genband.kandy.api.services.chats.IKandyMediaItem;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.IKandyVideoItem;
import com.genband.kandy.api.services.chats.KandyChatMessage;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.chats.KandyMessageBuilder;
import com.genband.kandy.api.services.chats.KandyMessageMediaItemType;
import com.genband.kandy.api.services.chats.KandySMSMessage;
import com.genband.kandy.api.services.chats.KandyTransferState;
import com.genband.kandy.api.services.common.KandyResponseCancelListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.tch.lingoine.R;
import com.tch.lingoine.adapters.MessagesAdapter;
import com.tch.lingoine.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Activity sample of Messaging functionality Implemented send/receive messages
 * methods
 */
public class ChatServiceActivity extends ActivityBase implements OnItemClickListener, KandyChatServiceNotificationListener {

    private static final String TAG = ChatServiceActivity.class.getSimpleName();

    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final int IMAGE_PICKER_RESULT = 1002;
    private static final int VIDEO_PICKER_RESULT = 1003;
    private static final int AUDIO_PICKER_RESULT = 1004;
    private static final int FILE_PICKER_RESULT = 1005;

    private MessagesAdapter mAdapter;
    private Queue<IKandyMessage> mMessagesUUIDQueue;
    private InputMethodManager mInputMethodManager;

    private View uiIncomingMsgsPullerButton;

    private EditText uiPhoneNumberEdit;
    private EditText uiMessageEdit;

    private MyMessage mSelectedMessage = null;
    private boolean isGroupChatMode;

    public static final String INCOMING_CALL = "INCOMING_CALL";
    public static final String CALL_NUMBER = "CALL_NUMBER";

	/*----------- Activtity initializations ----------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_chats);

        Intent i = getIntent();

        initViews();
        isGroupChatMode = false;
        setToolbar();

        boolean isIncomingCall = getIntent().getBooleanExtra(INCOMING_CALL, true);
        if (!isIncomingCall) {
            String phoneNumber = i.getStringExtra(CALL_NUMBER);
            if (phoneNumber == null || phoneNumber.contentEquals("null") || phoneNumber.isEmpty()) {
                finish();
            }
            uiPhoneNumberEdit.setText(phoneNumber + "@lingoine.gmail.com");
        } else {
            ;
        }
        setupHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerChatsNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterChatsNotifications();
    }

    /**
     * Show the popup menu with action to be done with the message
     */
    private void showMessageActionMenu() {

        final CharSequence[] actions;

        //for incoming and not acked messages will be shown the message ack action
        if (mSelectedMessage.kandyMessage.isIncoming() && !mSelectedMessage.isMsgAcked) {

            if (isDownloading(mSelectedMessage.kandyMessage)) {
                actions = new CharSequence[]{
                    getString(R.string.activity_chats_message_click_menu_cancel_download),
                    getString(R.string.activity_chats_message_click_menu_ack),
                    getString(R.string.activity_chats_message_click_menu_cancel)
                };
            } else {
                actions = new CharSequence[]{
                    getString(R.string.activity_chats_message_click_menu_view),
                    getString(R.string.activity_chats_message_click_menu_ack),
                    getString(R.string.activity_chats_message_click_menu_cancel)
                };
            }
        } else {
            if (isDownloading(mSelectedMessage.kandyMessage)) {
                actions = new CharSequence[]{
                    getString(R.string.activity_chats_message_click_menu_cancel_download),
                    getString(R.string.activity_chats_message_click_menu_cancel)
                };
            } else {
                actions = new CharSequence[]{
                    getString(R.string.activity_chats_message_click_menu_view),
                    getString(R.string.activity_chats_message_click_menu_cancel)
                };
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.activity_chats_message_click_menu_title)
            .setItems(actions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (actions[which].equals(getString(R.string.activity_chats_message_click_menu_view))) {
                        viewMessage();
                    } else if (actions[which].equals(getString(R.string.activity_chats_message_click_menu_ack))) {
                        markMsgAsReceived(mSelectedMessage.kandyMessage);
                        mSelectedMessage = null;
                    } else if (actions[which].equals(getString(R.string.activity_chats_message_click_menu_cancel_download))) {
                        cancelDownloadProcess();
                    } else {
                        dialog.dismiss();
                        mSelectedMessage = null;
                    }
                }
            });

        builder.create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    sendContact(data.getData());
                    break;

                case IMAGE_PICKER_RESULT:
                    sendImage(data.getData());
                    break;

                case VIDEO_PICKER_RESULT:
                    sendVideo(data.getData());
                    break;

                case AUDIO_PICKER_RESULT:
                    sendAudio(data.getData());
                    break;

                case FILE_PICKER_RESULT:
                    sendFile(data.getData());
                    break;

            }
        }
    }

    /**
     * Pick image by android default gallery picker
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_RESULT);
    }

    /**
     * Pick contact by android default contact picker
     */
    public void pickContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    /**
     * Pick audio by android default audio picker
     */
    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, AUDIO_PICKER_RESULT);
    }

    /**
     * Pick video by android default video picker
     */
    private void pickVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_PICKER_RESULT);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_PICKER_RESULT);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show(); }
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

	/*----------- Activtity initializations ----------*/

    private void initViews() {

        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ListView uiMessagesListView = (ListView) findViewById(R.id.activity_chats_messages_list);
        uiMessagesListView.setOnItemClickListener(ChatServiceActivity.this);

        mMessagesUUIDQueue = new LinkedBlockingQueue<IKandyMessage>();
        mAdapter = new MessagesAdapter(this, R.layout.message_listview_item, new ArrayList<MyMessage>());
        uiMessagesListView.setAdapter(mAdapter);

        View uiSendButton = findViewById(R.id.activirty_chats_send_msg_button);
        uiSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendText();
            }
        });

        uiIncomingMsgsPullerButton = findViewById(R.id.activirty_chats_get_incoming_msgs_button);
        uiIncomingMsgsPullerButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pullPendingEvents();
            }
        });

        Button uiSendImgButton = (Button) findViewById(R.id.activity_chats_img_button);
        uiSendImgButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        uiPhoneNumberEdit = (EditText) findViewById(R.id.activity_cahts_phone_number_edit);
        uiMessageEdit = (EditText) findViewById(R.id.activity_cahts_message_edit);

        Button uiSendContactButton = (Button) findViewById(R.id.activity_chats_contact_button);
        uiSendContactButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickContact();
            }
        });

        Button uiSendAudioButton = (Button) findViewById(R.id.activity_chats_audio_button);
        uiSendAudioButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                pickAudio();
            }
        });

        Button uiSendVideoButton = (Button) findViewById(R.id.activity_chats_video_button);
        uiSendVideoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickVideo();
            }
        });

        Button uiSendFileButton = (Button) findViewById(R.id.activity_chats_file_button);
        uiSendFileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickFile();
            }
        });

        Button uiSendLocationButton = (Button) findViewById(R.id.activity_chats_location_button);
        uiSendLocationButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCurrentLocation();
            }
        });

        Button uiSendSMSButton = (Button) findViewById(R.id.activity_chats_send_sms_button);
        uiSendSMSButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });

    }

	/*------- Functionality logic implementations -------*/

    /**
     * Register for the chat notifications
     */
    private void registerChatsNotifications() {

        Log.d(TAG, "registerNotifications");
        Kandy.getServices().getChatService().registerNotificationListener(this);
    }

    private void unRegisterChatsNotifications() {
        Log.d(TAG, "unRegisterNotifications");
        Kandy.getServices().getChatService().unregisterNotificationListener(this);
    }

    /**
     * Mark the message as delivered on UI thread include the notifyDataSetChanged call
     */
    private void markAsDeliveredOnUI(final KandyDeliveryAck message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.markAsDelivered(message);
            }
        });
    }

    /**
     * Mark the message as received and if success - call notifyDataSetChanged also
     */
    private void markAsReceivedOnUI(final IKandyMessage message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mAdapter.markAsReceived(message)) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Add message to list if this message doesn't exists in list. run on UI Thread and calls notifyDataSetChanged
     *
     * @param message
     */
    private void addUniqueMessageOnUI(final IKandyMessage message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.addUniqe(new MyMessage(message));
            }
        });
    }

    /**
     * Add message to the listview from UI thread include notifyDataSetChanged call
     *
     * @param message
     */
    private void addMessageOnUI(final IKandyMessage message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                uiMessageEdit.setText("");
                closeKeyboard();
                mAdapter.add(new MyMessage(message));
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void closeKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(uiMessageEdit.getWindowToken(), 0);
    }

    private KandyRecord getRecipient(String destination) {
        KandyRecord recipient = null;

        try {
            if (isGroupChatMode) {
                recipient = new KandyRecord(destination, KandyRecordType.GROUP);
            } else {
                recipient = new KandyRecord(destination);
            }
        } catch (KandyIllegalArgumentException ex) {
            UIUtils.showDialogWithErrorMessage(this, getString(R.string.activity_chat_phone_number_verification_text));
            return null;
        }

        return recipient;
    }

    private String getMessageText() {
        return uiMessageEdit.getText().toString();
    }

    private String getDestination() {
        return uiPhoneNumberEdit.getText().toString();
    }


    private void showCancelableUploadProgressDialog(final IKandyMessage message, String textMEssage) {

        UIUtils.showCancelableProgressDialogOnUiThreadWithProgress(this, textMEssage, new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Kandy.getServices().getChatService().cancelMediaTransfer(message, new KandyResponseCancelListener() {

                    @Override
                    public void onRequestFailed(int responseCode, String err) {
                        Log.d(TAG, "onRequestFailed: " + " responseCode: " + responseCode + " error: " + err);
                    }

                    @Override
                    public void onCancelSucceded() {
                        Log.d(TAG, "onCancelSucceded: " + "upload canceled");
                    }
                });

                dialog.dismiss();
            }
        });
    }

    /**
     * Send the audio file
     *
     * @param audioUri
     */
    private void sendAudio(Uri audioUri) {
        String destination = getDestination();
        String text = getMessageText();

        IKandyAudioItem kandyAudio = null;
        try {
            kandyAudio = KandyMessageBuilder.createAudio(text, audioUri);
        } catch (KandyIllegalArgumentException e) {
            Log.d(TAG, "sendAudio: " + e.getLocalizedMessage(), e);
        }
        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        final KandyChatMessage message = new KandyChatMessage(recipient, kandyAudio);

        showCancelableUploadProgressDialog(message, getString(R.string.msg_uploading_audio));

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyAudioFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.i(TAG, "uploadKandyAudioFile():onProgressUpdate(): " + progress + "%");
                UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "uploadKandyAudioFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }

    /**
     * Send the contact - vcard file
     *
     * @param contactUri
     */
    private void sendContact(Uri contactUri) {

        String destination = getDestination();
        String text = getMessageText();

        IKandyContactItem kandyContact = null;

        try {
            kandyContact = KandyMessageBuilder.createContact(text, contactUri);
        } catch (KandyIllegalArgumentException e) {
            Log.d(TAG, "sendContact: " + e.getLocalizedMessage(), e);
        }
        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        final KandyChatMessage message = new KandyChatMessage(recipient, kandyContact);

        showCancelableUploadProgressDialog(message, getString(R.string.msg_uploading_contact));

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyContactFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.i(TAG, "uploadKandyContactFile():onProgressUpdate(): " + progress + "%");
                UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "uploadKandyContactFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }

    /**
     * Send video
     *
     * @param videoUri uri with "content://" scheme also coud be passed uri of the file
     */
    private void sendVideo(Uri videoUri) {

        String destination = getDestination();
        String text = getMessageText();

        IKandyVideoItem kandyAudio = null;
        try {
            kandyAudio = KandyMessageBuilder.createVideo(text, videoUri);
        } catch (KandyIllegalArgumentException e) {
            Log.d(TAG, "sendVideo: " + e.getLocalizedMessage(), e);
        }
        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        final KandyChatMessage message = new KandyChatMessage(recipient, kandyAudio);

        showCancelableUploadProgressDialog(message, getString(R.string.msg_uploading_video));

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyVideoFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.d(TAG, "uploadKandyVideoFile():onProgressUpdate(): " + progress + "%");
                UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
            }

            @Override
            public void onRequestSucceded() {
                Log.d(TAG, "uploadKandyVideoFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }


    /**
     * Retrieves and send current device location via KandySDK
     */
    private void sendCurrentLocation() {

        try {

            Kandy.getServices().getLocationService().getCurrentLocation(new KandyCurrentLocationListener() {

                @Override
                public void onCurrentLocationReceived(Location location) {
                    Log.d(TAG, "onCurrentLocationReceived: lat: " + location.getLatitude() + " lon: " + location.getLongitude());
                    sendLocation(location);
                }

                @Override
                public void onCurrentLocationFailed(int errorCode, String error) {
                    Log.d(TAG, "onCurrentLocationFailed: errorCode: " + errorCode + " error: " + error);
                    UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, error);
                }
            });

        } catch (KandyIllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG, "initViews:sendCurrentLocation(); " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Send location
     *
     * @param location location to be sent
     */
    private void sendLocation(Location location) {
        String destination = getDestination();
        String text = getMessageText();

        IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation(text, location);
        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        final KandyChatMessage message = new KandyChatMessage(recipient, kandyLocation);

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyLocationFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.i(TAG, "uploadKandyLocationFile():onProgressUpdate(): " + progress + "%");
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "uploadKandyLocationFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }

    /**
     * Send image
     *
     * @param imgUri uri with "content://" scheme also coud be passed uri of the file
     */
    private void sendImage(Uri imgUri) {

        String destination = getDestination();
        String text = getMessageText();

        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        IKandyImageItem kandyImage = null;
        try {
            kandyImage = KandyMessageBuilder.createImage(text, imgUri);
        } catch (KandyIllegalArgumentException e) {
            Log.d(TAG, "sendImage: " + e.getLocalizedMessage(), e);
        }
        final KandyChatMessage message = new KandyChatMessage(recipient, kandyImage);

        showCancelableUploadProgressDialog(message, getString(R.string.msg_uploading_image));

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.i(TAG, "uploadKandyFile():onProgressUpdate(): " + progress + "%");
                UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "uploadKandyFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }

    /**
     * Send file
     *
     * @param fileUri uri with "content://" scheme also coud be passed uri of the file
     */
    private void sendFile(Uri fileUri) {

        String destination = getDestination();
        String text = getMessageText();

        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        IKandyFileItem kandyFile = null;
        try {
            kandyFile = KandyMessageBuilder.createFile(text, fileUri);
        } catch (KandyIllegalArgumentException e) {
            Log.d(TAG, "sendFile: " + e.getLocalizedMessage(), e);
        }
        final KandyChatMessage message = new KandyChatMessage(recipient, kandyFile);

        showCancelableUploadProgressDialog(message, getString(R.string.msg_uploading_image));

        Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "uploadKandyFile():onRequestFailed " + err + " error code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress progress) {
                Log.i(TAG, "uploadKandyFile():onProgressUpdate(): " + progress + "%");
                UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "uploadKandyFile():onRequestSucceded()");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_upload_succeed));
                addMessageOnUI(message);
            }
        });
    }

    /**
     * Send the message
     */
    private void sendText() {
        String destination = getDestination();
        String text = getMessageText();
        JSONObject additionalData = createJSONStub();

        // Set the recipient
        KandyRecord recipient = getRecipient(destination);

        if (recipient == null)
            return;

        // creating message to be sent
        final KandyChatMessage message = new KandyChatMessage(recipient, text);

        // set custome additional data to be sent with the message
        message.getMediaItem().setAdditionalData(additionalData);

        // Sending message
        Kandy.getServices().getChatService().sendChat(message, new KandyResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "Kandy.getChatService().sendMessage:onRequestFailed - Error: " + err + "\nResponse code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "Kandy.getChatService().sendMessage:onRequestSucceded - Message sent");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.activity_cahts_message_sent_label));
                addMessageOnUI(message);
            }
        });
    }

    private void sendSMS() {
        String destination = getDestination();
        String text = getMessageText();

        if (text.equals("") || text == null) {
            UIUtils.showDialogOnUiThread(this, getString(R.string.activity_cahts_message_invalid_title),
                getString(R.string.activity_cahts_message_empty_message));
            return;
        }

        final KandySMSMessage message;

        try {
            message = new KandySMSMessage(destination, "Kandy SMS", text);
        } catch (KandyIllegalArgumentException e) {
            UIUtils.handleResultOnUiThread(this, true, getString(R.string.activity_cahts_message_invalid_phone));
            Log.e(TAG, "sendSMS: " + " " + e.getLocalizedMessage(), e);
            return;
        }

        // Sending message
        Kandy.getServices().getChatService().sendSMS(message, new KandyResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "Kandy.getChatService().sendSMS:onRequestFailed - Error: " + err + "\nResponse code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "Kandy.getChatService().sendSMS:onRequestSucceded - Message sent");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.activity_cahts_message_sent_label));
                addMessageOnUI(message);
            }
        });
    }

    private JSONObject createJSONStub() {
        JSONObject additionalData = new JSONObject();
        try {
            additionalData.put("firstName", "John");
            additionalData.put("lastName", "Doe");
            additionalData.put("number", "+972523466332");
            additionalData.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            Log.w(TAG, "createJSONStub: " + e.getLocalizedMessage());
        }

        return additionalData;
    }

    /**
     * Send ack to sever for received/handled message
     *
     * @param message The received message
     */
    private void markMsgAsReceived(final IKandyMessage message) {

        message.markAsReceived(new KandyResponseListener() {
            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.e(TAG, "Kandy.getEventsService().ackEvent:onRequestFailed error: "
                    + err + ".Response code: " + responseCode);
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "Kandy.getEventsService().ackEvent:onRequestSucceded");
                UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, "Ack has been sent");
                markAsReceivedOnUI(message);
            }
        });
    }

    private void cancelDownloadProcess() {
        Kandy.getServices().getChatService().cancelMediaTransfer(mSelectedMessage.kandyMessage, new KandyResponseCancelListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                Log.d(TAG, "cancelDownloadProcess(): onRequestFailed: code: " + responseCode + " error: " + err);
            }

            @Override
            public void onCancelSucceded() {
                Log.d(TAG, "cancelDownloadProcess():onCancelSucceded()");
            }
        });
    }

    private void viewMessage() {
        IKandyMessage kandyMessage = (IKandyMessage) mSelectedMessage.kandyMessage;
        IKandyMediaItem kandyMediaItem = kandyMessage.getMediaItem();

        KandyMessageMediaItemType mediaType = mSelectedMessage.kandyMessage.getMediaItem().getMediaItemType();
        switch (mediaType) {
            case AUDIO:
            case FILE:
            case IMAGE:
            case CONTACT:
            case VIDEO:
                downloadAndOpenKandyFile((KandyChatMessage) kandyMessage);
                break;

            case LOCATION:
                Location location = ((IKandyLocationItem) kandyMediaItem).getLocation();
                openLocation(location);
                break;

            case TEXT:
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mSelectedMessage = mAdapter.getItem(position);
        showMessageActionMenu();
    }

    /**
     * Open received location in default applicatio (google maps)
     *
     * @param location
     */
    private void openLocation(Location location) {
        String locUri = String.format(Locale.ENGLISH, "geo:%f,%f", location.getLatitude(), location.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(locUri));
        startActivity(intent);
    }


    private boolean isDownloading(IKandyMessage message) {
        if ((KandyMessageMediaItemType.AUDIO.equals(message.getMediaItem().getMediaItemType())) ||
            (KandyMessageMediaItemType.VIDEO.equals(message.getMediaItem().getMediaItemType())) ||
            (KandyMessageMediaItemType.FILE.equals(message.getMediaItem().getMediaItemType())) ||
            (KandyMessageMediaItemType.IMAGE.equals(message.getMediaItem().getMediaItemType())) ||
            (KandyMessageMediaItemType.CONTACT.equals(message.getMediaItem().getMediaItemType()))) {

            IKandyFileItem item = (IKandyFileItem) message.getMediaItem();


            KandyTransferState state = item.getTransferProgress().getState();
            if (KandyTransferState.IN_PROGRESS == state) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Open downloaded media in default app, if media isn't downloaded yet will download it
     *
     * @param message
     */
    private void downloadAndOpenKandyFile(final KandyChatMessage message) {

        final Uri uri = ((IKandyFileItem) message.getMediaItem()).getLocalDataUri();

        if (uri != null) {

            final File f = new File(uri.getPath());

            if (!f.exists()) {
                // download the media if not exists
                Kandy.getServices().getChatService().downloadMedia(message, new KandyResponseProgressListener() {

                    @Override
                    public void onRequestFailed(int responseCode, String err) {
                        Log.e(TAG, "onRequestFailed: " + err + " response code: " + responseCode);
                    }

                    @Override
                    public void onRequestSucceded(Uri fileUri) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, message.getMediaItem().getMimeType());
                        startActivity(intent);
                    }

                    @Override
                    public void onProgressUpdate(IKandyTransferProgress progress) {
                        Log.d(TAG, "onProgressUpdate: " + " progress: " + progress);
                    }
                });
            } else {
                // launch an application to display the media to the user
                Log.d(TAG, "downloadAndOpenKandyFile: " + "message with UUID: " + message.getUUID().toString() + " with uri: " + uri.toString());

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, message.getMediaItem().getMimeType());

                startActivity(intent);
            }
        } else {

            Kandy.getServices().getChatService().downloadMedia(message, new KandyResponseProgressListener() {

                @Override
                public void onRequestFailed(int responseCode, String err) {
                    Log.e(TAG, "onRequestFailed: " + err + " response code: " + responseCode);
                }

                @Override
                public void onRequestSucceded(Uri fileUri) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, message.getMediaItem().getMimeType());
                    startActivity(intent);
                }

                @Override
                public void onProgressUpdate(IKandyTransferProgress progress) {
                    Log.d(TAG, "onProgressUpdate: " + " progress: " + progress);
                }
            });
        }
    }

    public void setupHandler() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                pullPendingEvents();
                handler.postDelayed(this, 5000);
            }
        });
    }

    /**
     * Pull the incoming pending messages
     */
    private void pullPendingEvents() {

        // UIUtils.showProgressDialogOnUiThread(this, getString(R.string.msg_pull_events));
        Kandy.getServices().getChatService().pullEvents(new KandyResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
                // UIUtils.handleResultOnUiThread(ChatServiceActivity.this, true, err);
                Log.e(TAG, "Kandy.getServices().getChatService().pullEvents:onRequestFailed: " + err + " response code: " + responseCode);
            }

            @Override
            public void onRequestSucceded() {
                Log.i(TAG, "Kandy.getServices().getChatService().pullEvents:onRequestSucceded");
                // UIUtils.handleResultOnUiThread(ChatServiceActivity.this, false, getString(R.string.msg_pull_events_succeed));
            }
        });
    }

    @Override
    public void onChatReceived(IKandyMessage message, KandyRecordType type) {
        Log.d(TAG, "onChatReceived: message: " + message + " type: " + type);
        addUniqueMessageOnUI((KandyChatMessage) message);
        mMessagesUUIDQueue.add((KandyChatMessage) message);
    }

    @Override
    public void onChatDelivered(KandyDeliveryAck message) {
        Log.d(TAG, "ChatsActivity:onChatDelivered " + message.getEventType().name() + " uuid: " + message.getUUID());
        markAsDeliveredOnUI(message);
    }

    @Override
    public void onChatMediaAutoDownloadFailed(IKandyMessage message, int errorCode, String err) {
        Log.d(TAG, "onChatMediaDownloadFailed: messageUUID: " + message.getUUID() + " error: " + err + " error code: " + errorCode);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        });

    }

    @Override
    public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress progress) {
        Log.d(TAG, "onChatMediaDownloadProgress: messageUUID: " + message.getUUID().toString() + " progress: " + progress);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }
        });
    }

    @Override
    public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri path) {
        Log.d(TAG, "onChatMediaDownloadSucceded: messageUUID: " + message.getUUID() + " uri path: " + path.getPath());
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        });
    }

    public class MyMessage {
        public IKandyMessage kandyMessage;
        public boolean isMsgDelivered;
        public boolean isMsgAcked;
        public Uri thumbnail;

        public MyMessage(IKandyMessage message) {
            kandyMessage = message;
            isMsgAcked = false;
            isMsgDelivered = false;
        }
    }
}