package com.faymosInc.wayapaylib.constants;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestObject {
    public String amount;
    public String description;
    public int currency;
    public int fee;
    public Customer customer;
    public String merchantId;
    public String wayaPublicKey;
}
