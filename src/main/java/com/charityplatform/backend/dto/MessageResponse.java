package com.charityplatform.backend.dto;

import org.aspectj.bridge.IMessage;

public class MessageResponse {

    private String message;
    private boolean success;


    public MessageResponse() {}
    public MessageResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
