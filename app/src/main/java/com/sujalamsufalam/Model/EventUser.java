package com.sujalamsufalam.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by nanostuffs on 03-02-2018.
 */

public class EventUser implements Parcelable,Comparable<EventUser> {
    private String role;
    private String userName;
    private String userID;
    private Boolean isUserSelected;

    public Boolean getUserSelected() {
        return isUserSelected;
    }

    public void setUserSelected(Boolean userSelected) {
        isUserSelected = userSelected;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public EventUser() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.role);
        dest.writeValue(this.isUserSelected);
        dest.writeString(this.userName);
        dest.writeString(this.userID);
    }

    protected EventUser(Parcel in) {
        this.role = in.readString();
        this.isUserSelected = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.userName = in.readString();
        this.userID = in.readString();
    }

    public static final Creator<EventUser> CREATOR = new Creator<EventUser>() {
        @Override
        public EventUser createFromParcel(Parcel source) {
            return new EventUser(source);
        }

        @Override
        public EventUser[] newArray(int size) {
            return new EventUser[size];
        }
    };

    // for sorting in attendance
    @Override
    public int compareTo(@NonNull EventUser o) {
        if(o.getUserSelected())
            return 1;
        else
            return -1;
    }
}
