package com.fsanitize.bluetalk;

public class UserChatInfo{
    private String address;
    private String nickname;
    private int n_messages;

    public String getAddress() {
        return address;
    }

    public String getNickname() {
        return nickname;
    }

    public int getN_messages() {
        return n_messages;
    }

    public UserChatInfo(String address, String nickname, int n_messages) {
        this.address = address;
        this.nickname = nickname;
        this.n_messages = n_messages;
    }
}