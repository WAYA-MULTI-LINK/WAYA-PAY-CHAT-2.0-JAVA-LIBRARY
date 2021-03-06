package com.faymosInc.wayapaylib.constants;

public class Enums {
    private final static String BASE_URL = "https://services.staging.wayapay.ng";

    public static final String testUrlPayment = BASE_URL + "/payment-gateway/api/v1/request/transaction";
    public static final String liveUrlPayment = BASE_URL + "";
    public static final String testUrlQuery =BASE_URL + "/payment-gateway/api/v1/reference/query/";
    public static final String liveUrlQuery =BASE_URL + "";
    public static final String aurUrlTest = "https://pay.staging.wayapay.ng/?_tranId=";
    public static final String aurUrlLive = "https://pay.staging.wayapay.ng/?_tranId=";

}
