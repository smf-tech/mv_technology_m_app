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
    }

    public LeavesModel() {
    }

    protected LeavesModel(Parcel in) {
        this.Id = in.readString();
        this.fromDate = in.readString();
        this.toDate = in.readString();
        this.reason = in.readString();
        this.typeOfLeaves = in.readString();
        this.status = in.readString();
    }

    public static final Parcelable.Creator<LeavesModel> CREATOR = new Parcelable.Creator<LeavesModel>() {
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
