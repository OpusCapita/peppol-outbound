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

}
