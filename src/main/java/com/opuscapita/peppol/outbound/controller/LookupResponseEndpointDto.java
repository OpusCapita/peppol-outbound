package com.opuscapita.peppol.outbound.controller;

import java.util.Date;

public class LookupResponseEndpointDto {

    private String address;
    private String transportProfile;
    private String certificateSubject;
    private Date certificateValidityStartDate;
    private Date certificateValidityEndDate;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTransportProfile() {
        return transportProfile;
    }

    public void setTransportProfile(String transportProfile) {
        this.transportProfile = transportProfile;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
    }

    public Date getCertificateValidityStartDate() {
        return certificateValidityStartDate;
    }

    public void setCertificateValidityStartDate(Date certificateValidityStartDate) {
        this.certificateValidityStartDate = certificateValidityStartDate;
    }

    public Date getCertificateValidityEndDate() {
        return certificateValidityEndDate;
    }

    public void setCertificateValidityEndDate(Date certificateValidityEndDate) {
        this.certificateValidityEndDate = certificateValidityEndDate;
    }
}
