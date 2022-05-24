package com.faymosInc.wayapaylib.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faymosInc.wayapaylib.constants.*;
import org.json.JSONObject;

public class PaymentTransaction {
    private Connections connections;

    public ResponseData initiatePayment(String amount, String description, String name, String email, String phoneNumber, String merchantId, String wayaPublicKey, String mode) throws JsonProcessingException {

        this.connections = new Connections(mode.equalsIgnoreCase("live") ? Enums.liveUrlPayment : Enums.testUrlPayment,null);

        Customer customer = Customer.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
        RequestObject requestObject = RequestObject.builder()
                .amount(amount)
                .currency(566)
                .merchantId(merchantId)
                .customer(customer)
                .description(description)
                .fee(1)
                .wayaPublicKey(wayaPublicKey)
                .build();
        JSONObject sss = this.connections.post(requestObject);
        ObjectMapper mapper = new ObjectMapper();
        ResponseDto ss = mapper.readValue(sss.toString(),ResponseDto.class);
        return ResponseData.builder()
                .Status(ss.status)
                .message(ss.message)
                .transId(ss.data.tranId)
                .authUrl(mode.equalsIgnoreCase("live")? Enums.aurUrlLive+ss.data.tranId:Enums.aurUrlTest+ss.data.tranId)
                .build();
    }

    public JSONObject queryPayment(String tranId, String mode){
        this.connections = new Connections(mode.equalsIgnoreCase("live")? Enums.liveUrlQuery:Enums.testUrlQuery, tranId);
        return this.connections.get();
    }
}

