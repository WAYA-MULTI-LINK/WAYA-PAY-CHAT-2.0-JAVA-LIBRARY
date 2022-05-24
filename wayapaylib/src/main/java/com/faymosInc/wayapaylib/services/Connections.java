package com.faymosInc.wayapaylib.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faymosInc.wayapaylib.constants.RequestMapping;
import com.faymosInc.wayapaylib.constants.RequestObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONObject;
import org.springframework.lang.Nullable;


public class Connections {
    private final String url;
    private final String tranId;

    public Connections(String url, @Nullable String tranId) {
        this.url = url;
        this.tranId = tranId;

    }
    public JSONObject post(RequestObject request) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(request);
        try{
            HttpResponse<JsonNode> response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .body(jsonString)
                    .asJson();
            return response.getBody().getObject();

        } catch (UnirestException e) {
             e.printStackTrace();
        }
        return null;
    }

    public JSONObject get(){
        try{
            HttpResponse<JsonNode> response = Unirest.get(url+tranId)
                    .header("Content-Type", "application/json")
                    .asJson();
        return response.getBody().getObject();
    }catch (UnirestException e){
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject get(RequestMapping request){
        try{
            HttpResponse<JsonNode> response = Unirest.get(url+tranId)
                    .header("Content-Type", "application/json")
                    .queryString(request.getParams())
                    .asJson();
            return response.getBody().getObject();
        }catch (UnirestException e){
            e.printStackTrace();
        }
        return  null;
    }
}
