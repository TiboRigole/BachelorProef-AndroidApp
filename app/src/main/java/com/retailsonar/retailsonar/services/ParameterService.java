package com.retailsonar.retailsonar.services;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by aaron on 4/7/2018.
 *
 * alle requests naar server in verband met parameter
 */

public interface ParameterService {
    /**
     * opvragen van alle eigenschappen van parameters
     *
     * @param parameter waarvoor eigenschappen gevraagd worden
     * @return JsonObject met alle eigenschappen
     */
    @GET("/ProtoREST/rest_example/parameter_service/eigenschappen")
    Call<JsonObject> getEigenschappen(@Query("parameter") String parameter);
}
