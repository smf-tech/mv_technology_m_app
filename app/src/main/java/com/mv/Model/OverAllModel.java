package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nanostuffs on 23-02-2018.
 */

public class OverAllModel {


    @SerializedName("submittedCount")
    @Expose
    private Integer submittedCount;
    @SerializedName("key")
    @Expose
    private String talukaName;
    @SerializedName("expectedCount")
    @Expose
    private Integer expectedCount;

    public Integer getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(Integer submittedCount) {
        this.submittedCount = submittedCount;
    }

    public String getTalukaName() {
        return talukaName;
    }

    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Integer getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(Integer expectedCount) {
        this.expectedCount = expectedCount;
    }


}
