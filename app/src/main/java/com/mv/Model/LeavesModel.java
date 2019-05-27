package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

import java.io.Serializable;

/**
 * Created by nanostuffs on 19-03-2018.
 */

@Entity(tableName = Constants.TABLE_LEAVES)
public class LeavesModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "unique_Id")
    private int Unique_Id;
    @ColumnInfo(name = "Id")
    @SerializedName("Id")
    @Expose
    private String Id;
    @ColumnInfo(name = "From__c")
    @SerializedName("From__c")
    @Expose
    private String fromDate;
    @ColumnInfo(name = "To__c")
    @SerializedName("To__c")
    @Expose
    private String toDate;
    @ColumnInfo(name = "Reason__c")
    @SerializedName("Reason__c")
    @Expose
    private String reason;
    @ColumnInfo(name = "Leave_Type__c")
    @SerializedName("Leave_Type__c")
    @Expose
    private String typeOfLeaves;
    @ColumnInfo(name = "Status__c")
    @SerializedName("Status__c")
    @Expose
    private String status;
    @ColumnInfo(name = "isHalfDay__c")
    @SerializedName("isHalfDay__c")
    @Expose
    private boolean isHalfDayLeave;

    public int getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(int unique_Id) {
        Unique_Id = unique_Id;
    }

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

    public boolean isHalfDayLeave() {
        return isHalfDayLeave;
    }

    public void setHalfDayLeave(boolean halfDayLeave) {
        isHalfDayLeave = halfDayLeave;
    }

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
        dest.writeString(this.comment);
        dest.writeString(this.Requested_User_Name__c);
        dest.writeString(this.Requested_User__c);
        dest.writeString(this.mv_user);
        dest.writeByte((byte)(this.isHalfDayLeave ? 1 : 0));
    }

    protected LeavesModel(Parcel in) {
        this.Id = in.readString();
        this.fromDate = in.readString();
        this.toDate = in.readString();
        this.reason = in.readString();
        this.typeOfLeaves = in.readString();
        this.status = in.readString();
        this.comment = in.readString();
        this.Requested_User_Name__c = in.readString();
        this.Requested_User__c = in.readString();
        this.mv_user = in.readString();
        this.isHalfDayLeave = in.readByte() !=0;
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
