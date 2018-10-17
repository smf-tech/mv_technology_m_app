package com.sujalamsufalam.Model;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sujalamsufalam.Utils.PreferenceHelper;

/**
 * Created by acer on 8/8/2017.
 */


public class User {


    private static User currentUser = null;

    public static User getCurrentUser(Context context) {
        PreferenceHelper mPreferenceHelper;

        if (currentUser == null) {
            mPreferenceHelper = new PreferenceHelper(context);
            if (TextUtils.isEmpty(mPreferenceHelper.getString(PreferenceHelper.UserData)))
                currentUser = new User();
            else {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                currentUser = gson.fromJson(mPreferenceHelper.getString(PreferenceHelper.UserData), User.class);
            }
        }
        return currentUser;
    }

    public static void clearUser() {
        currentUser = null;
    }

    @SerializedName("mvUser")
    @Expose
    private UserInfo mvUser = new UserInfo();

    @SerializedName("mvr")
    @Expose
    private UserInfo rolePermssion = new UserInfo();

    @SerializedName("mac")
    @Expose
    private UserInfo appConfig = new UserInfo();

    public UserInfo getExtendedUser() {
        return ExtendedUser;
    }

    public void setExtendedUser(UserInfo extendedUser) {
        ExtendedUser = extendedUser;
    }

    @SerializedName("eup")
    @Expose
    private UserInfo ExtendedUser = new UserInfo();

    public String getDuplicateMobileNo() {
        return duplicateMobileNo;
    }

    public void setDuplicateMobileNo(String duplicateMobileNo) {
        this.duplicateMobileNo = duplicateMobileNo;
    }

    @SerializedName("duplicateMobileNo")
    @Expose
    private String duplicateMobileNo;

    public UserInfo getMvUser() {
        return mvUser;
    }

    public void setMvUser(UserInfo mvUser) {
        this.mvUser = mvUser;
    }

    public UserInfo getRolePermssion() {
        return rolePermssion;
    }

    public void setRolePermssion(UserInfo rolePermssion) {
        this.rolePermssion = rolePermssion;
    }

    public UserInfo getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(UserInfo appConfig) {
        this.appConfig = appConfig;
    }


}