package com.faymosInc.wayapaylib.constants;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseData {
    private boolean Status;
    private String message;
    private String authUrl;
    private String transId;
}
