package com.faymosInc.wayapaylib.constants;

import java.util.HashMap;

public class RequestMapping {
    private HashMap<String, Object> objectHashMap;
    public RequestMapping(){
        this.objectHashMap = new HashMap<String, Object>();
    }
    public void addParams(String key, Object value){
        this.objectHashMap.put(key, value);
    }
    public HashMap<String, Object> getParams(){
        return  this.objectHashMap;
    }
}
