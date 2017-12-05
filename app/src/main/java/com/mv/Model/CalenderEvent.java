package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nanostuffs on 05-12-2017.
 */

public class CalenderEvent {
    @SerializedName("Id")
    @Expose
    String id;
    @SerializedName("Description__c")
    @Expose
    String description;
    @SerializedName("Date__c")
    @Expose
    String date;
    @SerializedName("MV_User1__c")
    @Expose
    String MV_User1__c;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMV_User1__c() {
        return MV_User1__c;
    }

    public void setMV_User1__c(String MV_User1__c) {
        this.MV_User1__c = MV_User1__c;
    }


}
