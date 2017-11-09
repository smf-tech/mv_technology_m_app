package com.mv.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mv.Utils.Constants;

/**
 * Created by nanostuffs on 27-10-2017.
 */
@Entity(tableName = Constants.TABLE_CONTAINER)
public class TaskContainerModel {
    @PrimaryKey()
    @ColumnInfo(name = "unique_Id")
    private String Unique_Id;
    public String getUnique_Id() {
        return Unique_Id;
    }

    public void setUnique_Id(String unique_Id) {
        Unique_Id = unique_Id;
    }


    public String getTaskListString() {
        return taskListString;
    }

    public void setTaskListString(String taskListString) {
        this.taskListString = taskListString;
    }

    @ColumnInfo(name = "taskListString")
    public String taskListString;
    public String getIsSave() {
        return isSave;
    }

    public void setIsSave(String isSave) {
        this.isSave = isSave;
    }

    @ColumnInfo(name = "isSave")
    @SerializedName("isSave ")
    @Expose
    private String isSave;
    @ColumnInfo(name = "MV_Process__c")
    @SerializedName("MV_Process__c")
    @Expose
    private String MV_Process__c;
    public String getMV_Process__c() {
        return MV_Process__c;
    }

    public void setMV_Process__c(String MV_Process__c) {
        this.MV_Process__c = MV_Process__c;
    }

    public String getTaskType() {
        return TaskType;
    }

    public void setTaskType(String taskType) {
        TaskType = taskType;
    }
    @ColumnInfo(name = "TaskType")
    @SerializedName("TaskType")
    @Expose
    private String TaskType;



}
