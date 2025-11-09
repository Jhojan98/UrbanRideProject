package com.movilidadsostenible.viaje_service.models;

import java.util.Date;

public class User {
    private Integer userCc;

    private String username;

    private String hashedPassword;
    private String firstName;
    private String secondName;
    private String firstLastname;
    private String secondLastname;

    private Date userBirthday;

    private String userEmail;

    private String subscriptionType;

    private Date userRegistrationDate;

    private boolean isVerified;

    private Integer otp;
}
