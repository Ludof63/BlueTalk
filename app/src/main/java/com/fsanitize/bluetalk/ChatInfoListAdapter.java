package com.fsanitize.bluetalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatInfoListAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<UserChatInfo> userInfoList;
    private BlueTalkHistory historyManager;

    public ChatInfoListAdapter(Context context, List<UserChatInfo> userInfoList,BlueTalkHistory historyManager) {
        this.context = context;
        this.userInfoList = userInfoList;
        this.historyManager = historyManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.chat_list_item),parent,false);
        return  new InfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserChatInfo userChatInfo = userInfoList.get(position);
        String nickname = userChatInfo.getNickname() != null?userChatInfo.getNickname():userChatInfo.getAddress();

        ((InfoHolder) holder).nicknameText.setText(nickname);
        ((InfoHolder) holder).messagesText.setText(String.valueOf(userChatInfo.getN_messages())+ " messages");

        ((InfoHolder) holder).buttonDelete.setOnClickListener(view ->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Chat History");
            builder.setMessage("Do you want to delete all messages with " + nickname + "? ");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    historyManager.removeUserHistory(userChatInfo.getAddress());

                    UserChatInfo theRemovedItem = userInfoList.get(holder.getAdapterPosition());
                    userInfoList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    private class InfoHolder extends RecyclerView.ViewHolder {
        TextView nicknameText, messagesText;
        Button buttonDelete;

        InfoHolder(View itemView) {
            super(itemView);
            nicknameText = itemView.findViewById(R.id.text_nickname_chatH_item);
            messagesText = itemView.findViewById(R.id.text_messages_chatH_item);
            buttonDelete = itemView.findViewById(R.id.button_delete_chatH_item);
        }
    }
}