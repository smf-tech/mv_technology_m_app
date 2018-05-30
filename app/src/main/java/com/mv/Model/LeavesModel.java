package com.mv.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nanostuffs on 19-03-2018.
 */

public class LeavesModel implements Parcelable {
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTypeOfLeaves() {
        return typeOfLeaves;
    }

    public void setTypeOfLeaves(String typeOfLeaves) {
        this.typeOfLeaves = typeOfLeaves;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String Id;
    private String fromDate;
    private String toDate;
    private String reason;
    private String typeOfLeaves;
    private String status;

    public String getIsHalfDayLeave() {
        return isHalfDayLeave;
    }

    public void setIsHalfDayLeave(String isHalfDayLeave) {
        this.isHalfDayLeave = isHalfDayLeave;
    }

    private String isHalfDayLeave;
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String comment;

    private String Requested_User_Name__c;

    public String getRequested_User_Name__c() {
        return Requested_User_Name__c;
    }

    public void setRequested_User_Name__c(String requested_User_Name__c) {
        Requested_User_Name__c = requested_User_Name__c;
    }

    public String getRequested_User__c() {
        return Requested_User__c;
    }

    public void setRequested_User__c(String requested_User__c) {
        Requested_User__c = requested_User__c;
    }

    private String Requested_User__c;

    public String getMv_user() {
        return mv_user;
    }

    public void setMv_user(String mv_user) {
        this.mv_user = mv_user;
    }

    private String mv_user;

    public LeavesModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Id);
        dest.writeString(this.fromDate);
        dest.writeString(this.toDate);
        dest.writeString(this.reason);
        dest.writeString(this.typeOfLeaves);
        dest.writeString(this.status);
        dest.writeString(this.isHalfDayLeave);
        dest.writeString(this.comment);
        dest.writeString(this.Requested_User_Name__c);
        dest.writeString(this.Requested_User__c);
        dest.writeString(this.mv_user);
    }

    protected LeavesModel(Parcel in) {
        this.Id = in.readString();
        this.fromDate = in.readString();
        this.toDate = in.readString();
        this.reason = in.readString();
        this.typeOfLeaves = in.readString();
        this.status = in.readString();
        this.isHalfDayLeave = in.readString();
        this.comment = in.readString();
        this.Requested_User_Name__c = in.readString();
        this.Requested_User__c = in.readString();
        this.mv_user = in.readString();
    }

    public static final Creator<LeavesModel> CREATOR = new Creator<LeavesModel>() {
        @Override
        public LeavesModel createFromParcel(Parcel source) {
            return new LeavesModel(source);
        }

        @Override
        public LeavesModel[] newArray(int size) {
            return new LeavesModel[size];
        }
    };
}
