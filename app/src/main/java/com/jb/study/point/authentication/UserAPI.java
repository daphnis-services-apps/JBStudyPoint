package com.jb.study.point.authentication;

import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class UserAPI {
    public static final String BASE_URL = "https://jbstudyapi.techvkt.com/api/";
    public static final String LOGIN = "login";
    public static final String REGISTER = "register";

    public interface GetUser{
        @Headers({"Accept: application/json"})
        @POST(REGISTER)
        public void insertUser(
                @Field("name") String name,
                @Field("email") String email,
                @Field("password") String password,
                Callback<Response> callback);

        //Call<User> getUserDetails(@Url String url);
    }

    private static GetUser getUser = null;

    public static GetUser getUserDetails(){
        if(getUser== null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            getUser = retrofit.create(GetUser.class);
        }
        return getUser;
    }
}
