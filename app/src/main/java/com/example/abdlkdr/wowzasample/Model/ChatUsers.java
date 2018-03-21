package com.example.abdlkdr.wowzasample.Model;

/**
 * Created by abdlkdr on 20.03.2018.
 */

public class ChatUsers {
    private String userUsername;
    private String toUserUsername;

    public ChatUsers() {
    }

    public ChatUsers(String userUsername, String toUserUsername) {
        this.userUsername = userUsername;
        this.toUserUsername = toUserUsername;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getToUserUsername() {
        return toUserUsername;
    }

    public void setToUserUsername(String toUserUsername) {
        this.toUserUsername = toUserUsername;
    }
}
