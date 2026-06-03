package com.faymosInc.wayapaylib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BvnVerificationRequest {

    /**
     * The 11-digit Bank Verification Number.
     */
    private String bvn;
}