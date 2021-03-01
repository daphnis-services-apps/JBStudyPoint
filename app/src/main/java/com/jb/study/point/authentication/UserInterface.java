package com.jb.study.point.authentication;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserInterface {

    String BASEURL = "https://jbstudyapi.techvkt.com/api/";

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("register")
    Call<String> getUserRegister(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("device_token") String device_token
    );

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("login")
    Call<String> getUserLogin(
            @Field("email") String uname,
            @Field("password") String password,
            @Field("device_token") String device_token
    );

    @Headers("Accept: application/json")
    @POST("getuser")
    @FormUrlEncoded
    Call<String> getCurrentUser(
            @Field("email") String email
    );

    @Headers("Accept: application/json")
    @POST("updateUser")
    @Multipart
    Call<String> getUpdatedUser(
           @Part MultipartBody.Part part,
           @Part("name") RequestBody name,
           @Part("subscription") RequestBody subscription,
           @Part("gender") RequestBody gender,
           @Part("dob") RequestBody dob,
           @Part("validity") RequestBody validity,
           @Part("email") RequestBody email);
}
