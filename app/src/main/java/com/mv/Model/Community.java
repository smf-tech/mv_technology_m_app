package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by Rohit Gujar on 13-09-2017.
 */
@Entity(tableName = Constants.TABLE_COMMUNITY)
public class Community {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;
    @ColumnInfo(name = "community_id")
    @SerializedName("community_id")
    @Expose
    private String Id;
    @ColumnInfo(name = "community_name")
    @SerializedName("community_name")
    @Expose
    private String Name;
    @ColumnInfo(name = "timestamp")
    @SerializedName("timestamp")
    @Expose
    private String Time;
    @ColumnInfo(name = "Count")
    @SerializedName("Count")
    @Expose
    private String Count;
    @ColumnInfo(name = "totalCount")
    @SerializedName("totalCount")
    @Expose
    private String TotalCount;
    @SerializedName("errorMsg")
    @Expose
    private String errorMsg;
    @ColumnInfo(name ="youCanPostInCommunity")
    @SerializedName("youCanPostInCommunity")
    @Expose
    private boolean CanPost;
    @ColumnInfo(name = "mute_notification")
    @SerializedName("mute_notification")
    @Expose
    private String muteNotification;

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    public String getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(String totalCount) {
        TotalCount = totalCount;
    }

    public boolean getCanPost() {
        return CanPost;
    }

    public void setCanPost(boolean canPost) {
        CanPost = canPost;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getMuteNotification() {
        return muteNotification;
    }

    public void setMuteNotification(String muteNotification) {
        this.muteNotification = muteNotification;
    }
}
