package com.example.abdlkdr.wowzasample.Model;

import java.io.Serializable;

/**
 * Created by abdlkdr on 5.03.2018.
 */

public class User  implements Serializable {
    String id;
    String username;
    String status;

    public User() {
    }

    public User(String id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
