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
    private ArrayList<Task> tasksList=new ArrayList<>();

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Id);
        dest.writeString(this.Name);
        dest.writeTypedList(this.tasksList);
    }

    public DashaBoardListModel() {
    }

    protected DashaBoardListModel(Parcel in) {
        this.Id = in.readString();
        this.Name = in.readString();
        this.tasksList = in.createTypedArrayList(Task.CREATOR);
    }

    public static final Parcelable.Creator<DashaBoardListModel> CREATOR = new Parcelable.Creator<DashaBoardListModel>() {
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
