package com.retailsonar.retailsonar.services;

import com.retailsonar.retailsonar.entities.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by aaron on 3/12/2018.
 */

public interface UserService {
    /**
     * Deze methode wordt uitgevoerd op de server
     * authenticatie van user
     *
     * @param login
     * @param password
     * @return Response met als body object van het type User
     *
     * Author: Aaron Hallaert
     */
   @POST("/ProtoREST/rest_example/authentication/login")
   Call<User> login(@Query("login") String login, @Query("password") String password);



    /**
     *
     * Deze methode wordt uitgevoerd op de server
     * huidige user opvragen aan de hand van token
     * @param authToken Bearer ...
     * @return Huidige user
     *
     * Author: Aaron Hallaert
     */
    @POST("/ProtoREST/rest_example/user_service/getUser")
    Call<User> getCurrentUser(@Header("Authorization") String authToken);



    /**
     *
     * Deze methode wordt uitgevoerd op de server
     * Token opslaan in database bij juiste user na inloggen op app
     *
     * @param login loginnaam van user
     * @param token token toegekend aan user
     * @return
     *
     * Author: Aaron Hallaert
     */
    @POST("/ProtoREST/rest_example/authentication/token")
    Call<User> setToken(@Query("login") String login,@Query("token") String token);


}
