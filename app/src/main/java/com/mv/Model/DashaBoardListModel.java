package com.mv.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by nanostuffs on 28-11-2017.
 */

public class DashaBoardListModel implements Parcelable {

    private String Id;
    private String Name;

    public String getMultiple_Role__c() {
        return Multiple_Role__c;
    }

    public void setMultiple_Role__c(String multiple_Role__c) {
        Multiple_Role__c = multiple_Role__c;
    }

    private String Multiple_Role__c;

    private ArrayList<Task> tasksList = new ArrayList<>();

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

    public ArrayList<Task> getTasksList() {
        return tasksList;
    }

    public void setTasksList(ArrayList<Task> tasksList) {
        this.tasksList = tasksList;
    }


    public DashaBoardListModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Id);
        dest.writeString(this.Name);
        dest.writeString(this.Multiple_Role__c);
        dest.writeTypedList(this.tasksList);
    }

    private DashaBoardListModel(Parcel in) {
        this.Id = in.readString();
        this.Name = in.readString();
        this.Multiple_Role__c = in.readString();
        this.tasksList = in.createTypedArrayList(Task.CREATOR);
    }

    public static final Creator<DashaBoardListModel> CREATOR = new Creator<DashaBoardListModel>() {
        @Override
        public DashaBoardListModel createFromParcel(Parcel source) {
            return new DashaBoardListModel(source);
        }

        @Override
        public DashaBoardListModel[] newArray(int size) {
            return new DashaBoardListModel[size];
        }
    };
}
