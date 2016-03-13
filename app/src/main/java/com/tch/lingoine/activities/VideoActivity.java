package com.tch.lingoine.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.audio.IKandyAudioDevice;
import com.genband.kandy.api.services.audio.KandyAudioDeviceType;
import com.genband.kandy.api.services.audio.KandyAudioServiceNotificationListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.IKandyOutgoingCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyCallTerminationReason;
import com.genband.kandy.api.services.calls.KandyOutgingVoipCallOptions;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyError;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.github.bijoysingh.starter.Functions;
import com.tch.lingoine.R;

import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends ActivityBase implements KandyCallServiceNotificationListener, KandyAudioServiceNotificationListener {

    private static final String TAG = VideoActivity.class.getSimpleName();

    private EditText uiPhoneNumberEdit;

    private ImageView uiCallButton;
    protected ImageView uiHangupButton;

    protected ToggleButton uiHoldTButton;
    protected ToggleButton uiMuteTButton;
    protected ToggleButton uiVideoTButton;
    protected ToggleButton uiSwitchCameraTButton;

    private CheckBox uiVideoCheckBox;
    private CheckBox uiPSTNCheckBox;
    private CheckBox uiSipTrunkCheckBox;

    protected KandyView uiRemoteVideoView;
    protected KandyView uiLocalVideoView;

    protected TextView uiAudioStateTextView;
    protected TextView uiVideoStateTextView;
    protected TextView uiCallsStateTextView;

    private AlertDialog mIncomingCallDialog;

    protected ToggleButton uiSpeakerButton;
    private ToggleButton uiBluethoothButton;
    private ToggleButton uiHeadphoneButton;
    private ToggleButton uiEarpieceButton;

    Boolean callDidStart = false;

    /**
     * Flag which indicates to start the call with video or without
     */
    protected boolean mIsCreateVideoCall = true;

    /**
     * Flag which indicates to start the call as PSTN out call
     */
    private boolean mIsPSTNCall = false;

    /**
     * Flag which indicates to start the call trough tha SIP Trunk
     */
    private boolean mIsSipTrunkCall = false;

    /**
     * Flag which indicates to hold and/or unhold the current call
     */
    private boolean mIsHold = false;

    /**
     * Flag which indicates if the call in mute mode or not
     */
    protected boolean mIsMute = false;

    /**
     * Flag which indicates to stop/continue sending video to recipient
     */
    private boolean mIsVideo = true;

    /**
     * Using for incoming and outgoing call, so pay attention to the <b>casting</b>.<br>
     * <b>For incoming call cast the instance to {@link IKandyIncomingCall}<br>
     * For outgoing call cast the instance to {@link IKandyOutgoingCall}</b>
     */
    protected IKandyCall mCurrentCall;

	/*-------- Initialization of buttons' listeners ----------*/

    private OnClickListener onCallButtonClicked = new OnClickListener() {

        @Override
        public void onClick(View v) {
            startCallProcess();
        }
    };

    protected OnClickListener onHangupButtonClicked = new OnClickListener() {

        @Override
        public void onClick(View v) {
            doHangup(mCurrentCall);
        }
    };

    protected OnCheckedChangeListener onHoldTButtonClicked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mCurrentCall != null) {
                switchHold(mCurrentCall, isChecked);
            }
        }
    };

    protected OnCheckedChangeListener onMuteTButtonClicked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mCurrentCall != null) {
                switchMute(mCurrentCall, isChecked);
            }
        }
    };

    protected OnCheckedChangeListener onVideoTButtonClicked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mCurrentCall != null) {
                switchVideo(mCurrentCall, isChecked);
            }
        }
    };
    protected OnCheckedChangeListener onSwitchCameraTButtonClicked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mCurrentCall != null) {
                switchCamera(mCurrentCall, isChecked);
            }
        }
    };

    private OnCheckedChangeListener onVideoCheckBoxChecked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIsCreateVideoCall = isChecked;
        }
    };

    private OnCheckedChangeListener onPSTNCheckBoxChecked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIsPSTNCall = isChecked;
        }
    };

    private OnCheckedChangeListener onSipTrunkCheckBoxChecked = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mIsSipTrunkCall = isChecked;
        }
    };

	/*----------- Activity's initializations -----------*/

    public static final String INCOMING_CALL = "INCOMING_CALL";
    public static final String CALL_NUMBER = "CALL_NUMBER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initViews();

        boolean isIncomingCall = getIntent().getBooleanExtra(INCOMING_CALL, true);
        if (!isIncomingCall) {
            String phoneNumber = getIntent().getStringExtra(CALL_NUMBER);
            setupPhoneNumber(phoneNumber);
            startCallProcess();
        } else if (HomeActivity.mIncomingCall != null) {
            createIncomingCallPopup(HomeActivity.mIncomingCall);
            HomeActivity.mIncomingCall = null;
        }

    }

    public void setupPhoneNumber(String number) {
        uiPhoneNumberEdit.setText(number);
    }

    protected void switchCamera(IKandyCall currentCall, boolean isChecked) {
        currentCall.getCameraForVideo();
        KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
        currentCall.switchCamera(cameraInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCallListener();
        registerAudioCallListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterCallListener();
        unregisterAudioCallListener();
    }

    @Override
    protected void onDestroy() {
        if (mCurrentCall != null) {
            doHangup(mCurrentCall);
        }

        super.onDestroy();

    }

    private void initViews() {

        uiPhoneNumberEdit = (EditText) findViewById(R.id.activity_calls_phone_number_edit);

        uiCallButton = (ImageView) findViewById(R.id.activity_calls_call_button);
        uiCallButton.setOnClickListener(onCallButtonClicked);

        uiHangupButton = (ImageView) findViewById(R.id.activity_calls_hangup_button);
        uiHangupButton.setOnClickListener(onHangupButtonClicked);

        uiHoldTButton = (ToggleButton) findViewById(R.id.activity_calls_hold_tbutton);
        uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);

        uiMuteTButton = (ToggleButton) findViewById(R.id.activity_calls_mute_tbutton);
        uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);

        uiVideoTButton = (ToggleButton) findViewById(R.id.activity_calls_video_tbutton);
        uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);


        uiSwitchCameraTButton = (ToggleButton) findViewById(R.id.activity_calls_switch_camera_tbutton);
        uiSwitchCameraTButton.setOnCheckedChangeListener(onSwitchCameraTButtonClicked);

        uiVideoCheckBox = (CheckBox) findViewById(R.id.activity_calls_video_checkbox);
        uiVideoCheckBox.setOnCheckedChangeListener(onVideoCheckBoxChecked);
        uiVideoCheckBox.setChecked(true);

        uiPSTNCheckBox = (CheckBox) findViewById(R.id.activity_calls_video_pstn_checkbox);
        uiPSTNCheckBox.setOnCheckedChangeListener(onPSTNCheckBoxChecked);

        uiSipTrunkCheckBox = (CheckBox) findViewById(R.id.activity_calls_video_sip_trunk_checkbox);
        uiSipTrunkCheckBox.setOnCheckedChangeListener(onSipTrunkCheckBoxChecked);

        uiAudioStateTextView = (TextView) findViewById(R.id.activity_calls_state_audio_text);
        setAudioState(mIsMute);

        uiVideoStateTextView = (TextView) findViewById(R.id.activity_calls_state_video_text);
        setVideoState(false, false);

        uiCallsStateTextView = (TextView) findViewById(R.id.activity_calls_state_call_text);
        setCallState(KandyCallState.INITIAL.name());

        uiRemoteVideoView = (KandyView) findViewById(R.id.activity_calls_video_view);
        uiLocalVideoView = (KandyView) findViewById(R.id.activity_calls_local_video_view);

        KandyCallState callState = mCurrentCall != null ? mCurrentCall.getCallState() : KandyCallState.INITIAL;
        setCallSettingsOnUIThread(callState);

        uiSpeakerButton = (ToggleButton) findViewById(R.id.activity_calls_call_route_speaker);
        uiBluethoothButton = (ToggleButton) findViewById(R.id.activity_calls_call_route_bluethooth);
        uiHeadphoneButton = (ToggleButton) findViewById(R.id.activity_calls_call_route_headphone);
        uiEarpieceButton = (ToggleButton) findViewById(R.id.activity_calls_call_route_earpiece);

        uiSpeakerButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onSpeakerClicked();

            }

            private void onSpeakerClicked() {
                Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.SPEAKER);
            }
        });

        uiBluethoothButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBluethoothClicked();

            }

            private void onBluethoothClicked() {
                Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.BLUETOOTH);
            }
        });

        uiHeadphoneButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onHeadphoneClicked();

            }

            private void onHeadphoneClicked() {
                Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.HEADPHONES);
            }
        });

        uiEarpieceButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onEarpieceClicked();

            }

            private void onEarpieceClicked() {
                Kandy.getServices().getAudioService().setAudioDevice(KandyAudioDeviceType.EARPIECE);
            }
        });

    }

	/*------------- Functionality logic --------------*/

    protected void setAudioState(boolean pState) {

        final String state = String.format("%s %s", getString(R.string.activity_calls_audio_state),
            String.valueOf(pState));
        setStateForTextViewOnUIThread(uiAudioStateTextView, state);
    }

    protected void setVideoState(boolean isReceiving, boolean isSending) {

        final String state = String.format("%s %s, %s %s", getString(R.string.activity_calls_receiving_video_state),
            String.valueOf(isReceiving), getString(R.string.activity_calls_sending_video_state), String.valueOf(isSending));
        setStateForTextViewOnUIThread(uiVideoStateTextView, state);
    }

    protected void setCallState(final String pState) {
        setStateForTextViewOnUIThread(uiCallsStateTextView, pState);
    }

    /**
     * Set the text for a TextView from the non UI thread
     *
     * @param pTextView where to set text
     * @param pState    state to be shown
     */
    private void setStateForTextViewOnUIThread(final TextView pTextView, final String pState) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pTextView.setText(pState);
            }
        });
    }

    private String getPhoneNumber() {
        return uiPhoneNumberEdit.getText().toString() + "@lingoine.gmail.com";
    }

    /**
     * Creates the popup for incoming call
     *
     * @param pInCall incoming call for which dialog will be created
     */
    private void createIncomingCallPopup(IKandyIncomingCall pInCall) {
        mCurrentCall = pInCall;

        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
        builder.setPositiveButton(getString(R.string.activity_calls_answer_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                accept();
                callDidStart = true;
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(getString(R.string.activity_calls_ignore_incoming_call_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ignoreIncomingCall((IKandyIncomingCall) mCurrentCall);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(R.string.activity_calls_reject_incoming_call_button_label), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectIncomingCall((IKandyIncomingCall) mCurrentCall);
                dialog.dismiss();
            }
        });

        builder.setMessage("You have an incoming call. Would you like to continue?");

        mIncomingCallDialog = builder.create();
        mIncomingCallDialog.show();
    }

    /**
     * Ignoring the incoming call -  the caller wont know about ignore, call will continue on his side
     *
     * @param pCall incoming call instance
     */
    public void ignoreIncomingCall(IKandyIncomingCall pCall) {
        if (pCall == null) {
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_hangup_text_msg));
            return;
        }

        pCall.ignore(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "mCurrentIncomingCall.ignore succeed");
                setCallSettingsOnUIThread(KandyCallState.TERMINATED);
                mCurrentCall = null;
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "mCurrentIncomingCall.ignore failed");
            }
        });
    }

    /**
     * Reject the incoming call
     *
     * @param pCall incoming call instance
     */
    public void rejectIncomingCall(IKandyIncomingCall pCall) {
        if (pCall == null) {
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_hangup_text_msg));
            return;
        }

        pCall.reject(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "mCurrentIncomingCall.reject succeeded");
                setCallSettingsOnUIThread(KandyCallState.TERMINATED);
                mCurrentCall = null;
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "mCurrentIncomingCall.reject. Error: " + err + "\nResponse code: " + responseCode);
            }
        });
    }

    /**
     * Accept incoming call
     */
    public void accept() {
        if (mCurrentCall.canReceiveVideo()) {
            //Call has video m line
            //so this call can be answered with
            //acceptCall(true) if one wants to answer with video
            // or
            //acceptCall(false) if one wants to answer with audio only

            ((IKandyIncomingCall) mCurrentCall).accept(mIsCreateVideoCall, new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    Log.i(TAG, "mCurrentIncomingCall.accept succeed");
                }

                @Override
                public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                    Log.i(TAG, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
                }
            });
        } else {
            //Call has only one m line, so this call will be answered
            //with only audio
            //acceptCall(false)

            ((IKandyIncomingCall) mCurrentCall).accept(false, new KandyCallResponseListener() {

                @Override
                public void onRequestSucceeded(IKandyCall call) {
                    Log.i(TAG, "mCurrentIncomingCall.accept succeed");
                }

                @Override
                public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                    Log.i(TAG, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
                }
            });
        }
    }

    /**
     * Register a listener to receive calls related events like incoming call, call state change, etc...
     */
    private void registerCallListener() {
        Log.d(TAG, "registerCallListener()");
        Kandy.getServices().getCallService().registerNotificationListener(this);
    }

    private void unregisterCallListener() {
        Log.d(TAG, "unregisterCallListener()");
        Kandy.getServices().getCallService().unregisterNotificationListener(this);
    }

    private void answerCall(final IKandyIncomingCall pCall) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                createIncomingCallPopup(pCall);
            }
        });
    }

    /**
     * Start calling process
     */
    private void startCallProcess() {
        doCall(getPhoneNumber());
    }

    /**
     * Do a call to entered phone number
     *
     * @param number user's phone with domain (username@domain) see {@link KandyRecord}
     */
    protected void doCall(String number) {

        if (mIsPSTNCall) {
            mCurrentCall = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
        } else if (mIsSipTrunkCall) {
            KandyRecord callee;
            try {
                number = KandyRecord.normalize(number);
                callee = new KandyRecord(number);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
                Functions.makeToast(this, getString(R.string.activity_chat_phone_number_verification_text));
                return;
            }
            mCurrentCall = Kandy.getServices().getCallService().createSIPTrunkCall(null, callee);
        } else {
            KandyRecord callee;
            try {
                callee = new KandyRecord(number);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
                Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
                Functions.makeToast(this, getString(R.string.activity_chat_phone_number_verification_text));
                return;
            }
            KandyOutgingVoipCallOptions callOptions = mIsCreateVideoCall ? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO : KandyOutgingVoipCallOptions.START_CALL_WITHOUT_VIDEO;
            mCurrentCall = Kandy.getServices().getCallService().createVoipCall(null, callee, callOptions);
        }

        if (mCurrentCall == null) {
            Functions.makeToast(VideoActivity.this, "Invalid number");
            return;
        }

        mCurrentCall.setLocalVideoView(uiLocalVideoView);
        mCurrentCall.setRemoteVideoView(uiRemoteVideoView);

        ((IKandyOutgoingCall) mCurrentCall).establish(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doCall:onRequestSucceeded: true");
                callDidStart = true;
                IKandyAudioDevice active = Kandy.getServices().getAudioService().getActiveAudioDevice();
                List<IKandyAudioDevice> available = Kandy.getServices().getAudioService().getAvailableAudioDevices();
                Log.i(TAG, "doCall:onRequestSucceeded: ActiveAudioDevice = " + active +
                    " AvailableAudioDevices = " + available);
                updateActiveAudioDevice(active);
                updateAvalibleAudioDevices(available);
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);
                Functions.makeToast(VideoActivity.this, err);
            }
        });
    }

    /**
     * Hanging up current call, and notify in case of fail.
     *
     * @param pCall current call for which hanguo will be applied
     */
    private void doHangup(IKandyCall pCall) {

        if (pCall == null) {
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_hangup_text_msg));
            return;
        }

        hangup();
    }

    /**
     * Raw call hangup
     */
    public void hangup() {
        mCurrentCall.hangup(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doHangup:onRequestSucceeded: true");
                mCurrentCall = null;
                setCallSettingsOnUIThread(KandyCallState.TERMINATED);
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "doHangup:onRequestFailed: " + err + " Response code: " + responseCode);
                Functions.makeToast(VideoActivity.this, err);
                mCurrentCall = null;
                setCallSettingsOnUIThread(KandyCallState.TERMINATED);
            }
        });

    }


    /**
     * Handle switching between mute/unmute mode of current call (if is active)
     *
     * @param isMute
     */
    private void switchMute(IKandyCall pCall, boolean isMute) {

        if (pCall == null) {
            uiMuteTButton.setChecked(false);
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_mute_call_text_msg));
            return;
        }

        mIsMute = isMute;

        if (mIsMute)
            doMute(pCall);
        else
            doUnMute(pCall);
    }

    /**
     * Mute current active call
     */
    private void doMute(IKandyCall pCall) {

        pCall.mute(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doMute:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "doMute:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    /**
     * Unmute current active call
     */
    private void doUnMute(IKandyCall pCall) {
        pCall.unmute(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doUnMute:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "doUnMute:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    /**
     * Handle switching between hold/unhold mode of current call (if is active)
     *
     * @param isHold
     */
    private void switchHold(IKandyCall pCall, boolean isHold) {

        if (pCall == null) {
            uiHoldTButton.setChecked(false);
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_hold_text_msg));
            return;
        }

        mIsHold = isHold;

        if (mIsHold)
            doHold(pCall);
        else
            doUnHold(pCall);
    }

    /**
     * Hold current active call
     */
    private void doHold(IKandyCall pCall) {
        pCall.hold(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doHold:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "doHold:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    /**
     * Unhold current active call
     */
    private void doUnHold(IKandyCall pCall) {
        pCall.unhold(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doUnHold:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "doUnHold:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    /**
     * Handle switching between start/stop video call in current call (if is active)
     *
     * @param isVideoOn
     */
    private void switchVideo(IKandyCall pCall, boolean isVideoOn) {
        if (pCall == null) {
            uiVideoTButton.setChecked(false);
            Functions.makeToast(VideoActivity.this, getString(R.string.activity_calls_invalid_video_call_text_msg));
            return;
        }

        if (isVideoOn)
            enableVideo(pCall);
        else
            disableVideo(pCall);
    }

    /**
     * Turn on video during video call
     */
    private void enableVideo(IKandyCall pCall) {
        pCall.startVideoSharing(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "enableVideo:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "enableVideo:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    /**
     * Turn off video during video call
     */
    private void disableVideo(IKandyCall pCall) {
        pCall.stopVideoSharing(new KandyCallResponseListener() {

            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "disableVideo:onRequestSucceeded: true");
            }

            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "disableVideo:onRequestFailed: " + err + " Response code: " + responseCode);
            }
        });
    }

    @Override
    public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo,
                                    boolean isSendingVideo) {
        Log.i(TAG, "onVideoStateChanged: Receiving: " + isReceivingVideo + " Sending: " + isSendingVideo);
        setVideoState(isReceivingVideo, isSendingVideo);
        setAudioState(call.isMute());
    }

    @Override
    public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
        //Here you can implement the GSMCallIncoming behavior/actions
        Log.i(TAG, "onGSMCallIncoming");
    }

    @Override
    public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
        //Here you can implement the GSMCallConnected behavior/actions
        Log.i(TAG, "onGSMCallConnected");
        doHold(mCurrentCall);
    }

    @Override
    public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
        //Here you can implement the GSMCallDisconnection behavior/actions
        Log.i(TAG, "onGSMCallDisconnected");
        doUnHold(mCurrentCall);
    }

    @Override
    public void onIncomingCall(IKandyIncomingCall call) {
        //Here while state is KandyCallState.RINGING you can play a ringtone
        Log.i(TAG, "onIncomingCall: " + call.getCallId());
        answerCall(call);
    }

    @Override
    public void onMissedCall(KandyMissedCallMessage call) {
        Log.d(TAG, "onMissedCall: call: " + call);
    }

    @Override
    public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage event) {
        Log.d(TAG, "onWaitingVoiceMailCall: event: " + event);

    }

    @Override
    public void onCallStateChanged(KandyCallState state, IKandyCall call) {
        //Here while state is KandyCallState.DIALING you can play a ringback
        Log.i(TAG, "onCallStatusChanged: " + state.name());

        setCallSettingsOnUIThread(state);

        String reason = "";
        if (state == KandyCallState.TALKING) {
            mCurrentCall.setLocalVideoView(uiLocalVideoView);
            mCurrentCall.setRemoteVideoView(uiRemoteVideoView);
        } else if (state == KandyCallState.TERMINATED) {
            KandyCallTerminationReason callTerminationReason = call.getTerminationReason();
            Log.d(TAG, "registerCallListener: " + "call TERMINATED reason: " + callTerminationReason);
            reason = "(" + callTerminationReason.getReason() + ")";
            mCurrentCall = null;
            if (mIncomingCallDialog != null) {
                mIncomingCallDialog.dismiss();
            }
            if (callDidStart) {
                showRatingAlert();
            } else {
                finish();
            }
        }
        setCallState(state.name() + reason);
    }

    public void showRatingAlert() {
        new AlertDialog.Builder(context)
            .setTitle("User Feedback")
            .setMessage("How do you rate the user?")
            .setPositiveButton("GOOD", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            })
            .setNegativeButton("TERRIBLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            })
            .setNeutralButton("IT WAS OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            })
            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            }).show();
    }

    ;


    /**
     * Set the states of video, hold and mute on ToggleButtons
     *
     * @param state
     */
    protected void setCallSettingsOnUIThread(KandyCallState state) {

        final boolean isMute;
        final boolean isVideoEnabled;
        final boolean isHold;
        final boolean isStartWithVideoEnabled;
        final boolean isSwitchCameraEnabled;
        final boolean isMuteEnabled;
        final boolean isHoldEnabled;
        final boolean isVideoButtonEnabled;

        switch (state) {
            case TERMINATED:
                isMute = false;
                isHold = false;
                isVideoEnabled = true;
                isStartWithVideoEnabled = true;
                isSwitchCameraEnabled = false;
                isMuteEnabled = false;
                isHoldEnabled = false;
                isVideoButtonEnabled = false;
                break;
            case TALKING:
                isMute = mIsMute;
                isHold = mIsHold;
                isVideoEnabled = mIsVideo;
                isStartWithVideoEnabled = false;
                isSwitchCameraEnabled = mCurrentCall.isSendingVideo();
                isMuteEnabled = true;
                isHoldEnabled = true;
                isVideoButtonEnabled = true;
                break;

            case INITIAL:
                isMute = false;
                isHold = false;
                isVideoEnabled = true;
                isStartWithVideoEnabled = true;
                isSwitchCameraEnabled = false;
                isMuteEnabled = false;
                isHoldEnabled = false;
                isVideoButtonEnabled = false;
                break;
            case ON_HOLD:
            case ON_DOUBLE_HOLD:
                isHold = true;
                isMute = false;
                isVideoEnabled = true;
                isStartWithVideoEnabled = true;
                isSwitchCameraEnabled = false;
                isMuteEnabled = false;
                isHoldEnabled = true;
                isVideoButtonEnabled = false;
                break;
            default:
                isMute = mIsMute;
                isHold = mIsHold;
                isVideoEnabled = mIsVideo;
                isStartWithVideoEnabled = false;
                isSwitchCameraEnabled = false;
                isMuteEnabled = false;
                isHoldEnabled = false;
                isVideoButtonEnabled = false;
                break;
        }

        mIsHold = isHold;
        mIsVideo = isVideoEnabled;
        mIsMute = isMute;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (uiHoldTButton != null) {
                    uiHoldTButton.setChecked(isHold);
                }
                if (uiMuteTButton != null) {
                    uiMuteTButton.setChecked(isMute);
                }
                if (uiVideoTButton != null) {
                    uiVideoTButton.setChecked(isVideoEnabled);
                }
                if (uiVideoCheckBox != null) {
                    uiVideoCheckBox.setEnabled(isStartWithVideoEnabled);
                }
                if (uiSwitchCameraTButton != null) {
                    uiSwitchCameraTButton.setEnabled(isSwitchCameraEnabled);
                }
                if (uiHoldTButton != null) {
                    uiHoldTButton.setEnabled(isHoldEnabled);
                }
                if (uiMuteTButton != null) {
                    uiMuteTButton.setEnabled(isMuteEnabled);
                }
                if (uiVideoTButton != null) {
                    uiVideoTButton.setEnabled(isVideoButtonEnabled);
                }
            }
        });
    }


    private void registerAudioCallListener() {
        Log.d(TAG, "registerAudioCallListener()");
        Kandy.getServices().getAudioService().registerNotificationListener(this);
    }

    private void unregisterAudioCallListener() {
        Log.d(TAG, "unregisterAudioCallListener()");
        Kandy.getServices().getAudioService().unregisterNotificationListener(this);
    }

    @Override
    public void onAudioRouteChanged(IKandyAudioDevice activeAudioDevice, KandyError error) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onAudioRouteChanged: " + "onAudioRouteChanged " + activeAudioDevice + " error = " + error);
        Functions.makeToast(VideoActivity.this, "onAudioRouteChanged " + activeAudioDevice + " error = " + error);

        updateActiveAudioDevice(activeAudioDevice);

    }

    private void updateActiveAudioDevice(IKandyAudioDevice activeAudioDevice) {
        uiSpeakerButton.setChecked(false);
        uiBluethoothButton.setChecked(false);
        uiHeadphoneButton.setChecked(false);
        uiEarpieceButton.setChecked(false);

        switch (activeAudioDevice.getType()) {
            case SPEAKER:
                uiSpeakerButton.setChecked(true);
                break;
            case BLUETOOTH:
                uiBluethoothButton.setChecked(true);
                break;
            case HEADPHONES:
                uiHeadphoneButton.setChecked(true);
                break;
            case EARPIECE:
                uiEarpieceButton.setChecked(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAvailableAudioRoutesChanged(ArrayList<IKandyAudioDevice> availableAudioDevices) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onAvailableAudioRoutesChanged: " + "onAvailableAudioRoutesChanged " + availableAudioDevices);
        updateAvalibleAudioDevices(availableAudioDevices);
    }

    private void updateAvalibleAudioDevices(List<IKandyAudioDevice> availableAudioDevices) {
        uiSpeakerButton.setEnabled(false);
        uiBluethoothButton.setEnabled(false);
        uiHeadphoneButton.setEnabled(false);
        uiEarpieceButton.setEnabled(false);

        for (IKandyAudioDevice audioDevice : availableAudioDevices) {
            switch (audioDevice.getType()) {
                case SPEAKER:
                    uiSpeakerButton.setEnabled(true);
                    break;
                case BLUETOOTH:
                    uiBluethoothButton.setEnabled(true);
                    break;
                case HEADPHONES:
                    uiHeadphoneButton.setEnabled(true);
                    break;
                case EARPIECE:
                    uiEarpieceButton.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isActiveAudioDevice(KandyAudioDeviceType type) {
        IKandyAudioDevice activeAudioDevice = Kandy.getServices().getAudioService().getActiveAudioDevice();
        if (activeAudioDevice.getType().equals(type)) {
            Log.d(TAG, "isActiveAudioDevice: " + "AudioDeviceType =" + type + " isAactive");

            return true;
        }
        return false;
    }

    private boolean isAvailableAudioDevice(KandyAudioDeviceType type) {
        List<IKandyAudioDevice> availableAudioDevice = Kandy.getServices().getAudioService().getAvailableAudioDevices();
        Log.d(TAG, "isAvailableAudioDevice: type = " + type + " availableAudioDevice = " + availableAudioDevice);
        for (IKandyAudioDevice kandyAudioDevice : availableAudioDevice) {
            if (kandyAudioDevice.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}

