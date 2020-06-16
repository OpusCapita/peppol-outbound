package com.opuscapita.peppol.outbound.controller;

import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;

import java.util.ArrayList;
import java.util.List;

public class LookupResponseDocumentTypeDto {

    private DocumentTypeIdentifier documentTypeIdentifier;
    private ProcessIdentifier processIdentifier;
    private List<LookupResponseEndpointDto> endpointList;

    public LookupResponseDocumentTypeDto() {
    }

    public LookupResponseDocumentTypeDto(DocumentTypeIdentifier documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
        this.endpointList = new ArrayList<>();
    }

    public DocumentTypeIdentifier getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public void setDocumentTypeIdentifier(DocumentTypeIdentifier documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
    }

    public ProcessIdentifier getProcessIdentifier() {
        return processIdentifier;
    }

    public void setProcessIdentifier(ProcessIdentifier processIdentifier) {
        this.processIdentifier = processIdentifier;
    }

    public List<LookupResponseEndpointDto> getEndpointList() {
        return endpointList;
    }

    public void setEndpointList(List<LookupResponseEndpointDto> endpointList) {
        this.endpointList = endpointList;
    }
}
