package com.mv.Retrofit;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Nonostuffs on 2/1/2017.
 */

public interface ServiceRequest {


    @FormUrlEncoded
    @POST("")
    Call<ResponseBody> loginSalesforce(@Url String url, @Field("username") String mUsername, @Field("password") String mPassword,
                                       @Field("client_secret") String mClientSecret, @Field("client_id") String ClientId,
                                       @Field("grant_type") String mGrantType, @Field("response_type") String mResponseType);

    @GET("")
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);

    @GET("")
    Call<ResponseBody> getSalesForceData(@Url String url);

    @POST("")
    Call<ResponseBody> sendDataToSalesforce(@Url String url, @Body JsonObject object);


    @DELETE("")
    Call<ResponseBody> deleteDataFromSalesforce(@Url String url);

    @POST("")
    Call<ResponseBody> sendImageToSalesforce(@Url String url, @Body JsonArray jsonArray);

    @FormUrlEncoded
    @POST("/services/apexrest/WS_DeleteComments")
    Call<ResponseBody> deleteComment(@Field("commentId") String commentId);

    @POST("user/app_get_state")
    Call<ResponseBody> getState();

    @FormUrlEncoded
    @POST("user/app_get_district_by_state")
    Call<ResponseBody> getDistrict(@Field("state") String mState);

    @FormUrlEncoded
    @POST("user/app_get_taluka")
    Call<ResponseBody> getTaluka(@Field("state") String mState, @Field("district") String mDistrict);

    @FormUrlEncoded
    @POST("user/app_get_cluster")
    Call<ResponseBody> getCluster(@Field("state") String mState, @Field("district") String mDistrict, @Field("taluka") String mTaluka);

    @FormUrlEncoded
    @POST("user/app_get_village")
    Call<ResponseBody> getVillage(@Field("state") String mState, @Field("district") String mDistrict, @Field("taluka") String mTaluka, @Field("cluster") String mCluster);

    @FormUrlEncoded
    @POST("user/app_get_school")
    Call<ResponseBody> getSchool(@Field("state") String mState, @Field("district") String mDistrict, @Field("taluka") String mTaluka, @Field("cluster") String mCluster, @Field("village") String mVillage);

    @FormUrlEncoded
    @POST("get_all_location")
    Call<ResponseBody> getAllLocation(@Field("state") String mState, @Field("district") String mDistrict);

    @FormUrlEncoded
    @POST("save_new_location")
    Call<ResponseBody> submitLocation(@Field("state") String mState, @Field("district") String mDistrict, @Field("taluka") String mTaluka, @Field("cluster") String mCluster, @Field("village") String mVillage, @Field("school_name") String mSchool);

    @FormUrlEncoded
    @POST("")
    Call<ResponseBody> getLoacationData(@Url String url, @Field("state") String mState, @Field("district") String mDistrict, @Field("taluka") String mTaluka, @Field("cluster") String mCluster, @Field("village") String mVillage);

    @FormUrlEncoded
    @POST("")
    Call<ResponseBody> getMapContent(@Url String url, @Field("id") String mId, @Field("lat") String mlat, @Field("lon") String mlon);

}
