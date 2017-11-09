package com.mv.Retrofit;


import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
    Call<ResponseBody> getSalesForceData(@Url String url);

    @POST("")
    Call<ResponseBody> sendDataToSalesforce(@Url String url, @Body JsonObject object);


    @POST("user/app_get_district")
    Call<ResponseBody> getDistrict();


}
