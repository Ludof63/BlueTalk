package com.fsanitize.bluetalk;

import static com.fsanitize.bluetalk.BluetoothBaseActivity.MY_ADDRESS;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BluetoothMessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_MESSAGE_SENT = 1;
    private static final int VIEW_MESSAGE_RECEIVED = 2;
    private Context context;
    private List<BluetoothMessage> messageList;

    public BluetoothMessageListAdapter(Context context, List<BluetoothMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
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
        TextView messageText, timeText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_chat_to_message);
            timeText = itemView.findViewById(R.id.text_chat_to_time);            }

        void bind(BluetoothMessage message) {
            messageText.setText(message.getMessage());
            timeText.setText(new SimpleDateFormat(context.getString(R.string.chat_time_pattern)).format(new Date(message.getCreatedAt())));
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_chat_from_message);
            timeText = itemView.findViewById(R.id.text_chat_from_time);
        }

        void bind(BluetoothMessage message) {
            messageText.setText(message.getMessage());
            timeText.setText(new SimpleDateFormat(context.getString(R.string.chat_time_pattern)).format(new Date(message.getCreatedAt())));
        }
    }
}