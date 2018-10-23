package com.mv.Model.MultiLocationModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by user on 10/19/2018.
 */

public class MultiLocationResponse {

    @SerializedName("ResultCode")
    @Expose
    private String resultCode;
    @SerializedName("ResultData")
    @Expose
    private List<MultiLocation> resultData = null;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public List<MultiLocation> getResultData() {
        return resultData;
    }

    public void setResultData(List<MultiLocation> resultData) {
        this.resultData = resultData;
    }
}
