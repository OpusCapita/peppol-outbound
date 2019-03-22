package com.opuscapita.peppol.outbound.errors;

public class OxalisError {

    private String mask;
    private OutboundError type;

    public OxalisError() {
    }

    public OxalisError(OutboundError type, String mask) {
        this.type = type;
        this.mask = mask;
    }

    public OutboundError getType() {
        return type;
    }

    public void setType(OutboundError type) {
        this.type = type;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

}
