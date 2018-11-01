package com.mv.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by user on 9/17/2018.
 */

public class MapUserData {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("lon")
    @Expose
    private double lon;
    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lastSeen")
    @Expose
    private String lastSeen;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
