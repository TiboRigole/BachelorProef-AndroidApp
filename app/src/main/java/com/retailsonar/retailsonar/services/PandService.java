package com.retailsonar.retailsonar.services;

import com.google.gson.JsonObject;
import com.retailsonar.retailsonar.entities.Pand;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by aaron on 3/24/2018.
 *
 * alle requests naar server in verband met panden
 */

public interface PandService {
    /**
     *
     * @param winkel
     * @return alle panden van bepaalde winkel
     */
    @GET("/ProtoREST/rest_example/pand_service/winkelPanden")
    Call<List<Pand>> getWinkelPanden(@Query("winkel") String winkel);


    /**
     *
     * @param regio
     * @param winkel
     * @return alle panden van bepaalde winkel EN regio
     */
    @GET("/ProtoREST/rest_example/pand_service/regioPanden")
    Call<List<Pand>> getRegioPanden(@Query("regio") String regio,@Query("winkel") String winkel );


    /**
     *
     * @param id id van pand
     * @return afbeelding Byte64 gecodeerd in JsonObject
     */
    @GET("/ProtoREST/rest_example/pand_service/afbeeldingPand")
    Call<JsonObject> getAfbeeldingPand(@Query("id") long id);


    /**
     *
     * @param winkel
     * @param regio
     * @return aantal incomplete panden van winkel EN regio
     */
    @GET("/ProtoREST/rest_example/pand_service/incompleted")
    Call<JsonObject> getAantalIncompletedWinkel(@Query("winkel") String winkel, @Query("regio") String regio );


    /**
     *
     * @param winkel
     * @return alle incomplete panden van winkel
     */
    @GET("/ProtoREST/rest_example/pand_service/incompleteWinkelPanden")
    Call<List<Pand>> getIncompleteWinkelPanden(@Query("winkel") String winkel);


    /**
     *
     * @param winkel
     * @param regio
     * @return alle effectieve incomplete panden van winkel EN regio
     */
    @GET("/ProtoREST/rest_example/pand_service/incompleteRegioPanden")
    Call<List<Pand>> getIncompleteRegioPanden(@Query("regio") String regio, @Query("winkel") String winkel);


    /**
     *
     * @param pandId
     * @return noodzakelijke parameters horend bij een pand met id pandID
     */
    @GET("/ProtoREST/rest_example/pand_service/parametersPand")
    Call<JsonObject> getNoodzakelijkeParamsPand(@Query("pandId") long pandId);


    /**
     * opvragen van pand met id id
     * @param id id van gewilde pand
     * @return Pand met id id
     */
    @GET("/ProtoREST/rest_example/pand_service/getPand")
    Call<Pand> getPandById(@Query("id") long id);


    /**
     * updaten pand
     *
     * @param p Pand die upgedate moet worden
     * @return niets
     */
    @PUT("/ProtoREST/rest_example/pand_service/updatePand")
    Call<String> updatePand(@Body Pand p);

    /**
     * afbeelding naar database bij pand opslaan
     *
     * @param object afbeelding Byte64 gecodeerd in JsonObject
     * @param pandId id van pand waar afbeelding toe behoort
     * @return niets
     */
    @POST("/ProtoREST/rest_example/pand_service/setAfbeelding")
    Call<Pand> setAfbeeldingPandId( @Body JsonObject object,@Query("pandId") long pandId);


}
