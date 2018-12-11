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

    @ColumnInfo(name = "taskListString")
    private String taskListString;

    public String getTaskListString() {
        return taskListString;
    }

    public void setTaskListString(String taskListString) {
        this.taskListString = taskListString;
    }

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

    @ColumnInfo(name = "proAnsListString")
    private String proAnsListString;

    public String getProAnsListString() {
        return proAnsListString;
    }

    public void setProAnsListString(String proAnsListString) {
        this.proAnsListString = proAnsListString;
    }

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

    public String getHeaderPosition() {
        return headerPosition;
    }

    public void setHeaderPosition(String headerPosition) {
        this.headerPosition = headerPosition;
    }

    @ColumnInfo(name = "headerPosition")
    @SerializedName("headerPosition")
    @Expose
    private String headerPosition;

    @ColumnInfo(name = "IsAllowDelete")
    private boolean IsDeleteAllow;

    public boolean getIsDeleteAllow() {
        return IsDeleteAllow;
    }

    public void setIsDeleteAllow(boolean deleteAllow) {
        IsDeleteAllow = deleteAllow;
    }

    public String getTaskTimeStamp() {
        return taskTimeStamp;
    }

    public void setTaskTimeStamp(String taskTimeStamp) {
        this.taskTimeStamp = taskTimeStamp;
    }

    @ColumnInfo(name = "taskTimeStamp")
    private String taskTimeStamp;

    @ColumnInfo(name = "FormCommentCount")
    @SerializedName("FormCommentCount")
    private String FormCommentCount;

    public String getFormCommentCount() {
        return FormCommentCount;
    }

    public void setFormCommentCount(String formCommentCount) {
        FormCommentCount = formCommentCount;
    }

    @ColumnInfo(name = "FormReadCommentCount")
    @SerializedName("FormReadCommentCount")
    private String FormReadCommentCount;

    public String getFormReadCommentCount() {
        return FormReadCommentCount;
    }

    public void setFormReadCommentCount(String formReadCommentCount) {
        FormReadCommentCount = formReadCommentCount;
    }
}
