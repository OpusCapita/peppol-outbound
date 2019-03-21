package com.opuscapita.peppol.outbound.errors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "peppol.outbound.errors")
public class OxalisErrorConfiguration {
    
    private List<OxalisError> list;

    public List<OxalisError> getList() {
        return list;
    }

    public void setList(List<OxalisError> oxalisErrors) {
        this.list = oxalisErrors;
    }

    class OxalisError {

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

}
