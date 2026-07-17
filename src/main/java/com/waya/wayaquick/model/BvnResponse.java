package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from {@code POST /identity-verification/bvn}. Contains the BVN holder's KYC record.
 *
 * <p>BVN data is sensitive personal information. Store, transmit, and log it only as your
 * data-protection obligations allow.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BvnResponse(
        /** 11-digit Bank Verification Number. */
        String bvn,
        /** First name as registered on the BVN. */
        String firstName,
        /** Middle name as registered on the BVN. */
        String middleName,
        /** Last name as registered on the BVN. */
        String lastName,
        /** Date of birth, e.g. "15-Aug-2001". */
        String dateOfBirth,
        /** Primary phone number linked to the BVN. */
        String phoneNumber1,
        /** Date the BVN was registered, e.g. "19-Nov-2018". */
        String registrationDate,
        /** Email address linked to the BVN. */
        String email,
        /** "Male" or "Female". */
        String gender,
        /** Local government area of origin. */
        String lgaOfOrigin,
        /** Local government area of residence. */
        String lgaOfResidence,
        /** Marital status, e.g. "Single". */
        String maritalStatus,
        /** Nationality, e.g. "Nigeria". */
        String nationality,
        /** Full residential address on record. */
        String residentialAddress,
        /** State of origin, e.g. "Enugu State". */
        String stateOfOrigin,
        /** "False" when clear. Treat anything other than "False" with care. */
        String watchListed,
        /** Base64-encoded portrait image. */
        String base64Image
) {
}
