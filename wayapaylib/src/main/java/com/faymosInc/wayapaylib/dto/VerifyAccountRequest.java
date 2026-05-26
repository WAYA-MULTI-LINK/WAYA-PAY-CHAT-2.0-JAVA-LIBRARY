package com.faymosInc.wayapaylib.dto;

import lombok.Data;

@Data
public class VerifyAccountRequest {

    private String accountNumber;
    private String bankCode;
}