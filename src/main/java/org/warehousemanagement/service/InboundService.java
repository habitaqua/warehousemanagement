package org.warehousemanagement.service;

import com.google.inject.Inject;
import org.warehousemanagement.sao.InboundDbSAO;

public class InboundService {

    private InboundDbSAO inboundDbSAO;

    @Inject
    public InboundService(InboundDbSAO inboundDbSAO) {
        this.inboundDbSAO = inboundDbSAO;
    }

}
