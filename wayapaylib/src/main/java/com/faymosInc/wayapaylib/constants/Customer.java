package com.faymosInc.wayapaylib.constants;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Customer {
    public String name;
    public String email;
    public String phoneNumber;
    public String customerId;
}
