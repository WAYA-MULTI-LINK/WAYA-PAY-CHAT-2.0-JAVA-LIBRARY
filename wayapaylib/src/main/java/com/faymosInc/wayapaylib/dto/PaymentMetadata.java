package com.faymosInc.wayapaylib.dto;


import lombok.Data;

@Data
public class PaymentMetadata {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String cancelUrl;
}