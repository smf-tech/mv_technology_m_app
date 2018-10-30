package com.sujalamsufalam.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Rohit Gujar on 04-10-2017.
 */

public class Comment {

    @SerializedName("Id")
    @Expose
    private
    String id;
    @SerializedName("userId")
    @Expose
    private
    String userId;
    @SerializedName("userName")
    @Expose
    private
    String userName;
    @SerializedName("userURLId")
    @Expose
    private
    String userUrl;
    @SerializedName("commnets")
    @Expose
    private
    String comment;
    @SerializedName("dateAndTime")
    @Expose
    private
    String time;

    @SerializedName("errorMsg")
    @Expose
    String errorMsg;

    @SerializedName("error")
    @Expose
    String error;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
