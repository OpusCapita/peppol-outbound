package com.opuscapita.peppol.outbound.controller;

import no.difi.vefa.peppol.common.model.ParticipantIdentifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LookupResponseDto implements Serializable {

    private String errorMessage;
    private ParticipantIdentifier participantIdentifier;
    private List<LookupResponseDocumentTypeDto> documentTypeList;

    public LookupResponseDto() {
    }

    public LookupResponseDto(ParticipantIdentifier participantIdentifier) {
        this.participantIdentifier = participantIdentifier;
        this.documentTypeList = new ArrayList<>();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ParticipantIdentifier getParticipantIdentifier() {
        return participantIdentifier;
    }

    public void setParticipantIdentifier(ParticipantIdentifier participantIdentifier) {
        this.participantIdentifier = participantIdentifier;
    }

    public List<LookupResponseDocumentTypeDto> getDocumentTypeList() {
        return documentTypeList;
    }

    public void setDocumentTypeList(List<LookupResponseDocumentTypeDto> documentTypeList) {
        this.documentTypeList = documentTypeList;
    }
}
