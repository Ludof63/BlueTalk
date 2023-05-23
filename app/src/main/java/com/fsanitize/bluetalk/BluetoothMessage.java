package com.fsanitize.bluetalk;

public class BluetoothMessage {
    private String message;
    private String senderAddress;
    private String senderNickname;
    private long createdAt;

    public BluetoothMessage(String message, String senderAddress, long createdAt) {
        this.message = message;
        this.senderAddress = senderAddress;
        this.createdAt = createdAt;
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

}
