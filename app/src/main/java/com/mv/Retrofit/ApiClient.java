package com.mv.Retrofit;


import android.content.Context;

import com.mv.BuildConfig;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.TLSSocketFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by User on 4/19/2017.
 */

public class ApiClient {
    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithHeader = null;

    public static Retrofit getClient() {
        if (retrofit == null) {


            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = null;

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
            Interceptor interceptor1 = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    final Request request = chain.request().newBuilder()
                           // .addHeader("Authorization", "OAuth " + new PreferenceHelper(context).getString(PreferenceHelper.AccessToken))
                            .addHeader("Authorization", "OAuth " + "00D0l0000000Q7W!AQoAQHEqkkzY2eqBoG7.cdnNvxNQfuaMRIr.PcTybD6Lx7skg5Ihk_8GL6sHWxLrUlPaO_nWpUj4p5Al2Kdmt71Xyk8iThMM")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .build();

                    return chain.proceed(request);
                }
            };


            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            Interceptor interceptor2 = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (response.code() == 403) {
                        // handleForbiddenResponse();
                    }
                    return response;
                }
            };
            OkHttpClient client = null;
            try {
                client = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory()).addInterceptor(interceptor).addInterceptor(interceptor2).addInterceptor(interceptor1).build();
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


}
