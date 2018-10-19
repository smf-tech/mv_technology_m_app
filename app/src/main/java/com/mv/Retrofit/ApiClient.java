package com.mv.Retrofit;

import android.content.Context;
import android.util.Log;

import com.mv.BuildConfig;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.Logger;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.TLSSocketFactory;
import com.mv.Utils.Utills;

import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by User on 4/19/2017.
 */
public class ApiClient {
    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithHeader = null;
    private static Retrofit retrofitImageHeader = null;

    public static Retrofit getImageClient() {
        if (retrofitImageHeader == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = null;

            try {
                client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            } catch (Exception e) {
                e.printStackTrace();
            }

            retrofitImageHeader = new Retrofit.Builder()
                    .baseUrl("http://mobileapp.mulyavardhan.org/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitImageHeader;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {

            OkHttpClient client = null;
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            try {
                client = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory()).addInterceptor(interceptor).build();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASEURL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClientWitHeader(final Context context) {

        if (retrofitWithHeader == null) {
            final PreferenceHelper preferenceHelper = new PreferenceHelper(context);
            Interceptor interceptor1 = chain -> {
                final Request request = chain.request().newBuilder()
                        .addHeader("Authorization", "OAuth " + new PreferenceHelper(context).getString(PreferenceHelper.AccessToken))
                        .addHeader("Content-Type", "application/json")
                        .build();

                return chain.proceed(request);
            };

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor interceptor2 = chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);
                if (response.code() == 401) {
                    loginToSalesforce(context, preferenceHelper);
                }
                return response;
            };

            OkHttpClient client = null;
            try {
                client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .sslSocketFactory(new TLSSocketFactory()).addInterceptor(interceptor).addInterceptor(interceptor2).addInterceptor(interceptor1).build();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }

            retrofitWithHeader = new Retrofit.Builder()
                    .baseUrl(new PreferenceHelper(context).getString(PreferenceHelper.InstanceUrl))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithHeader;
    }

    private static void loginToSalesforce(final Context context, final PreferenceHelper preferenceHelper) {

        ServiceRequest apiService = ApiClient.getClient().create(ServiceRequest.class);
        apiService.loginSalesforce(BuildConfig.LOGIN_URL, BuildConfig.USERNAME, BuildConfig.PASSWORD,
                BuildConfig.CLIENT_SECRET, BuildConfig.CLIENT_ID, Constants.GRANT_TYPE,
                Constants.RESPONSE_TYPE).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    String access_token = obj.getString("access_token");
                    String instance_url = obj.getString("instance_url");
                    String id = obj.getString("id");
                    String str_id = id.substring(id.lastIndexOf("/") + 1, id.length());

                    Log.e("$$$$$$$$$$", str_id);

                    preferenceHelper.insertString(PreferenceHelper.AccessToken, access_token);
                    preferenceHelper.insertString(PreferenceHelper.InstanceUrl, instance_url);
                    preferenceHelper.insertString(PreferenceHelper.SalesforceUserId, str_id);
                    preferenceHelper.insertString(PreferenceHelper.SalesforceUsername, BuildConfig.USERNAME);
                    preferenceHelper.insertString(PreferenceHelper.SalesforcePassword, BuildConfig.PASSWORD);

                    Utills.showToast("Please try again...", context);
                } catch (Exception e) {
                    Log.e("$$ loginSalesForce(): ", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.doToast(context.getString(R.string.error_something_went_wrong), context);
            }
        });
    }
}
