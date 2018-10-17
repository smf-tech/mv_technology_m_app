package com.mv.Model.location_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by user on 10/11/2018.
 */

public class MultiLocation {
    @SerializedName("villages_name")
    @Expose
    private String villagesName;
    @SerializedName("taluka_name")
    @Expose
    private String talukaName;
    @SerializedName("state_name")
    @Expose
    private String stateName;
    @SerializedName("district_name")
    @Expose
    private String districtName;

    public String getVillagesName() {
        return villagesName;
    }

    public void setVillagesName(String villagesName) {
        this.villagesName = villagesName;
    }

    public String getTalukaName() {
        return talukaName;
    }

    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

}
