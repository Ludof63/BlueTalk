package com.fsanitize.bluetalk.UI;

import static com.fsanitize.bluetalk.UI.BluetoothBaseActivity.MY_ADDRESS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fsanitize.bluetalk.Data.BluetoothMessage;
import com.fsanitize.bluetalk.Logic.SentimentAnalyzer;
import com.fsanitize.bluetalk.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BluetoothMessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_MESSAGE_SENT = 1;
    private static final int VIEW_MESSAGE_RECEIVED = 2;
    private static boolean SENTIMENT_ANALYSIS = true;
    private static SentimentAnalyzer sentimentAnalyzer;
    private static final String time_pattern ="dd-MM-yyyy @ HH:mm:ss";
    private Context context;
    private List<BluetoothMessage> messageList;

    public BluetoothMessageListAdapter(Context context, List<BluetoothMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        sentimentAnalyzer =  new SentimentAnalyzer(context);
    }

    public void enableSentimentAnalysis(){
        SENTIMENT_ANALYSIS = true;
    }

    public void disableSentimentAnalysis(){
        SENTIMENT_ANALYSIS = false;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getSenderAddress().equals(MY_ADDRESS))
            return VIEW_MESSAGE_SENT;

        return VIEW_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_from_me_item, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_to_me_item, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BluetoothMessage message = messageList.get(position);
        switch (holder.getItemViewType()){
            case VIEW_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, emoticonText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_chat_to_message);
            timeText = itemView.findViewById(R.id.text_chat_to_time);
            emoticonText = itemView.findViewById(R.id.text_chat_to_emoticon);
        }

        void bind(BluetoothMessage message) {
            messageText.setText(message.getMessage());
            timeText.setText(new SimpleDateFormat(time_pattern).format(new Date(message.getCreatedAt())));
            if(SENTIMENT_ANALYSIS)
                emoticonText.setText(getMessageSentiment(message.getMessage()));
            else
                emoticonText.setText("");
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, emoticonText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_nickname_chatH_item);
            timeText = itemView.findViewById(R.id.text_chat_from_time);
            emoticonText = itemView.findViewById(R.id.text_chat_from_emoticon);

        }

        void bind(BluetoothMessage message) {
            messageText.setText(message.getMessage());
            timeText.setText(new SimpleDateFormat(time_pattern).format(new Date(message.getCreatedAt())));
            if(SENTIMENT_ANALYSIS)
                emoticonText.setText(getMessageSentiment(message.getMessage()));
            else
                emoticonText.setText("");
        }
    }

    private String getMessageSentiment(String message){
        int extraPositiveEmoticon = 0x1F603;
        int positiveEmoticon = 0x1F642;
        int neutralEmoticon = 0x1F610;
        int negativeEmoticon = 0x1F641;
        int extraNegativeEmoticon = 0x1F621;

        switch (sentimentAnalyzer.getMessageSentimentStatus(message)){
            case SentimentAnalyzer.SENTIMENT_STATUS.EXTRA_POSITIVE:
                return new String(Character.toChars(extraPositiveEmoticon));
            case SentimentAnalyzer.SENTIMENT_STATUS.POSITIVE:
                return new String(Character.toChars(positiveEmoticon));
            case SentimentAnalyzer.SENTIMENT_STATUS.EXTRA_NEGATIVE:
                return new String(Character.toChars(extraNegativeEmoticon));
            case SentimentAnalyzer.SENTIMENT_STATUS.NEGATIVE:
                return new String(Character.toChars(negativeEmoticon));
            case SentimentAnalyzer.SENTIMENT_STATUS.NEUTRAL:
                return new String(Character.toChars(neutralEmoticon));
        }

        return "error";
    }
}