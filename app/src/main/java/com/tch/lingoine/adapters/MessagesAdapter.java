package com.tch.lingoine.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.IKandyContactItem;
import com.genband.kandy.api.services.chats.IKandyImageItem;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.IKandyVideoItem;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.chats.KandyMessageMediaItemType;
import com.genband.kandy.api.services.chats.KandyMessageType;
import com.genband.kandy.api.utils.KandyMediaUtils;
import com.tch.lingoine.R;
import com.tch.lingoine.activities.ChatServiceActivity;
import com.tch.lingoine.activities.ChatServiceActivity.MyMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Adapter for the {@link IKandyMessage} in the LIstView
 */
public class MessagesAdapter extends ArrayAdapter<ChatServiceActivity.MyMessage> {

    private static final String TAG = MessagesAdapter.class.getSimpleName();

    private SimpleDateFormat mDateFormat;

    public MessagesAdapter(Context context, int resource, List<ChatServiceActivity.MyMessage> items) {
        super(context, resource, items);
        mDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.message_listview_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.uiMessageText = (TextView) convertView.findViewById(R.id.ui_message_body_text);
            viewHolder.uiMessageDate = (TextView) convertView.findViewById(R.id.ui_message_body_time);
            viewHolder.uiNumberText = (TextView) convertView.findViewById(R.id.ui_message_sender_text);
            viewHolder.uiThumbnail = (ImageView) convertView.findViewById(R.id.ui_thumbnail);
            viewHolder.uiMsgDirection = (ImageView) convertView.findViewById(R.id.ui_message_direction);
            viewHolder.uiThumbnailVideoIndicator = (ImageView) convertView.findViewById(R.id.ui_thumbnail_video_indicator);
            viewHolder.uiThumbnailVideoDuration = (TextView) convertView.findViewById(R.id.ui_thumbnail_video_duration);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyMessage message = getItem(position);
        if (message != null) {

            setMessageDisplayName(viewHolder, message);
            setMessageStatus(convertView, message);

            boolean isIncoming = message.kandyMessage.isIncoming();
            setMessageDirection(viewHolder, isIncoming);

            String text = message.kandyMessage.getMediaItem().getMessage();
            setMessageText(viewHolder, text);

            long time = System.currentTimeMillis();
            Date data = new Date(time);
            String dateString = mDateFormat.format(data);
            setMessageDate(viewHolder, dateString);

            // get the custome additional data received with the message
            JSONObject additionalData = message.kandyMessage.getMediaItem().getAdditionalData();
            Log.d(TAG, "getView: " + " additionalData: " + additionalData);

            // This function is time consuming operation , in commercial application, consider handle it on non UI thread
            setMessageThumbnail(viewHolder, message.kandyMessage);

        }

        return convertView;
    }

    /**
     * Set image icon for incoming/outgoing message
     *
     * @param isIncoming
     */
    private void setMessageDirection(ViewHolder viewHolder, boolean isIncoming) {
        if (isIncoming) {
            viewHolder.uiMsgDirection.setImageResource(R.drawable.ic_incoming);
        } else {
            viewHolder.uiMsgDirection.setImageResource(R.drawable.ic_outgoing);
        }
    }

    /**
     * Set message text of the received/sent message
     *
     * @param viewHolder
     * @param text
     */
    private void setMessageText(ViewHolder viewHolder, String text) {
        viewHolder.uiMessageText.setText(text);
    }

    private void setMessageDate(ViewHolder viewHolder, String text) {
        viewHolder.uiMessageDate.setText(text);
    }

    /**
     * Set display name - number of sender if incoming message and recipient if outgoing
     *
     * @param message
     */
    private void setMessageDisplayName(ViewHolder viewHolder, MyMessage message) {
        boolean isIncoming = message.kandyMessage.isIncoming();
        String displayName;
        if (isIncoming) {
            displayName = message.kandyMessage.getSender().getUserName();
        } else {
            displayName = message.kandyMessage.getRecipient().getUserName();
        }

        viewHolder.uiNumberText.setText(displayName);
    }

    /**
     * Set status for message Received if incoming and Delivered for outgoing
     *
     * @param convertView
     * @param message
     */
    private void setMessageStatus(View convertView, MyMessage message) {
        convertView.setBackgroundColor(Color.WHITE);

        if (message.isMsgAcked)//if was sent ACK for received message
            convertView.setBackgroundColor(Color.GRAY);

        if (message.isMsgDelivered) { //if got ACK for sent message
            convertView.setBackgroundColor(Color.argb(0xaa, 0x00, 0xaa, 0x00));
        }
    }

    /**
     * Set the thumnail for message if there is no thumbnail received from server will put default thumnail or create. <br>
     * <b>Note:</b> This function is time consuming operation , in commercial application, consider handle it on non UI thread
     */
    private void setMessageThumbnail(ViewHolder viewHolder, IKandyMessage message) {
        KandyMessageMediaItemType mediaType = message.getMediaItem().getMediaItemType();
        KandyMessageType messageType = message.getMessageType();


        Uri thumbnailUri;
        Bitmap bitmap = null;

        viewHolder.uiThumbnailVideoIndicator.setVisibility(View.INVISIBLE);
        viewHolder.uiThumbnailVideoDuration.setVisibility(View.INVISIBLE);

        switch (mediaType) {

            case AUDIO:
                viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_volume_on);
                break;

            case LOCATION:
                viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_place);
                break;

            case TEXT:
                if (KandyMessageType.SMS.equals(messageType)) {
                    viewHolder.uiThumbnail.setImageResource(android.R.drawable.sym_action_email);
                } else {
                    viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_chat);
                }
                break;

            case FILE:
                viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_attachment);
                break;

            case IMAGE:
                IKandyImageItem kandyImage = (IKandyImageItem) message.getMediaItem();
                Log.i(TAG, "IMAGE: viewHolder.uiThumbnail.setImageBitmap(bitmap) called");

                if (kandyImage.getLocalThumbnailUri() == null) {
                    thumbnailUri = kandyImage.getLocalDataUri();
                } else {
                    thumbnailUri = kandyImage.getLocalThumbnailUri();
                }

                if (thumbnailUri != null) {
                    bitmap = KandyMediaUtils.getImageThumbnail(thumbnailUri);
                }

                if (bitmap != null) {
                    viewHolder.uiThumbnail.setImageBitmap(bitmap);
                } else {
                    viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_picture);
                }

                break;

            case CONTACT:
                IKandyContactItem kandyContact = (IKandyContactItem) message.getMediaItem();

                Bitmap bmp = kandyContact.getContactImage();

                if (bmp != null) {
                    viewHolder.uiThumbnail.setImageBitmap(bmp);
                } else {
                    viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_person);
                }

                break;

            case VIDEO:
                IKandyVideoItem kandyVideo = (IKandyVideoItem) message.getMediaItem();

                if (kandyVideo.getLocalThumbnailUri() == null) {

                    thumbnailUri = kandyVideo.getLocalDataUri();


                } else {
                    thumbnailUri = kandyVideo.getLocalThumbnailUri();
                }


                bitmap = KandyMediaUtils.getImageThumbnail(thumbnailUri);

                if (bitmap != null) {
                    viewHolder.uiThumbnailVideoIndicator.setVisibility(View.VISIBLE);

                    viewHolder.uiThumbnail.setImageBitmap(bitmap);
                } else {
                    viewHolder.uiThumbnail.setImageResource(R.drawable.ic_action_video);
                }

                long duration = kandyVideo.getDuration() / 1000;
                Log.d(TAG, "setMessageThumbnail: " + " Video duration: " + duration);

                if (duration > 0) {
                    String durationString = String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
                    viewHolder.uiThumbnailVideoDuration.setText(durationString);
                    viewHolder.uiThumbnailVideoDuration.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.uiThumbnailVideoDuration.setVisibility(View.INVISIBLE);
                }

                break;

            default:
                viewHolder.uiThumbnail.setVisibility(View.GONE);
                break;

        }
    }

    /**
     * Add message to the listview's dataset if message doesn't exists there
     *
     * @param myMessage
     */
    public void addUniqe(MyMessage myMessage) {
        IKandyMessage kandyMsgToAdd = myMessage.kandyMessage;
        for (int i = 0; i < getCount(); i++) {

            IKandyMessage kandyMessage = (IKandyMessage) getItem(i).kandyMessage;

            if (kandyMessage.isIncoming() && kandyMessage.getUUID().equals(kandyMsgToAdd.getUUID())) {
                return;
            }
        }

        add(myMessage);
        notifyDataSetChanged();
    }

    /**
     * Mark the message as received message on recipient side</br>
     * <b>Note:</b>
     * <i>The message which was marked as received on recipient's side will be marked as delivered on sender's side</i>
     *
     * @return
     */
    public boolean markAsReceived(IKandyMessage message) {

        for (int i = 0; i < this.getCount(); i++) {
            MyMessage msg = getItem(i);

            if (msg.kandyMessage.isIncoming() &&
                message.getUUID().equals(msg.kandyMessage.getUUID()) &&
                !msg.isMsgAcked) {

                getItem(i).isMsgAcked = true;

                return true;
            }
        }

        return false;
    }

    /**
     * Mark the message as delivered on the sender side </br>
     * <b>Note:</b>
     * <i>The message which was marked as received on recipient's side will be marked as delivered on sender's side</i>
     *
     * @param message
     */
    public void markAsDelivered(KandyDeliveryAck message) {
        UUID msgUUId = message.getUUID();

        for (int i = 0; i < this.getCount(); i++) {
            MyMessage msg = getItem(i);

            if (!msg.kandyMessage.isIncoming() &&
                msgUUId.equals(msg.kandyMessage.getUUID()) &&
                !msg.isMsgDelivered) {
                getItem(i).isMsgDelivered = true;
                notifyDataSetChanged();
                return;
            }
        }
    }

    static class ViewHolder {
        TextView uiNumberText;
        TextView uiMessageText;
        TextView uiMessageDate;
        ImageView uiThumbnail;
        ImageView uiMsgDirection;
        ImageView uiThumbnailVideoIndicator;
        TextView uiThumbnailVideoDuration;
    }


}

