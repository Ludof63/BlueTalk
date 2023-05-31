package com.fsanitize.bluetalk;

public class BluetoothMessage {
    private String message;
    private String senderAddress;
    private String nickname;
    private long createdAt;

    public BluetoothMessage(String message, String senderAddress, long createdAt, String nickname) {
        this.message = message;
        this.senderAddress = senderAddress;
        this.createdAt = createdAt;
        this.nickname = nickname;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
    public String getNickname() {return nickname;}

}
