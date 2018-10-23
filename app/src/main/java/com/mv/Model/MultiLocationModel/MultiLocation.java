package com.mv.Model.MultiLocationModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by user on 10/19/2018.
 */

public class MultiLocation {

    @SerializedName("taluka_name")
    @Expose
    private String talukaName;

    @SerializedName("state_name")
    @Expose
    private String stateName;

    @SerializedName("district_name")
    @Expose
    private String districtName;

    @SerializedName("cluster_name")
    @Expose
    private String clusterName;

    @SerializedName("villages_name")
    @Expose
    private String villageName;

    @SerializedName("school_name")
    @Expose
    private String schoolName;

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

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}
