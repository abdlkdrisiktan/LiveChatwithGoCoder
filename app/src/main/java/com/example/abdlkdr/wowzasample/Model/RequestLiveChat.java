package com.example.abdlkdr.wowzasample.Model;

/**
 * Created by abdlkdr on 6.03.2018.
 */

public class RequestLiveChat {
    private String id;
    //Programda kişiyi seçen kişi
    private User user;
    //Seçilen kişi ve konuşmak için aradığı kişi
    private User toUser;
    //Konuşma hala devam ediyor mu diye status tutuyoruz
    private String status;
    //Live chat'in url oluşturuyoruz
    private String liveChatUrl;
    //İsteği kabul edip etmediğini kontrol ediyoruz  //Yes or No
    private String isAccepted;

    public RequestLiveChat(String id, User user, User toUser, String status, String liveChatUrl, String isAccepted) {
        this.id = id;
        this.user = user;
        this.toUser = toUser;
        this.status = status;
        this.liveChatUrl = liveChatUrl;
        this.isAccepted = isAccepted;
    }

    public RequestLiveChat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLiveChatUrl() {
        return liveChatUrl;
    }

    public void setLiveChatUrl(String liveChatUrl) {
        this.liveChatUrl = liveChatUrl;
    }

    public String getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(String isAccepted) {
        this.isAccepted = isAccepted;
    }
}
