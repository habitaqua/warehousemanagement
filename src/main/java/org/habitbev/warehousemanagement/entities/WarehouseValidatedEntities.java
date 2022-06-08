package org.habitbev.warehousemanagement.entities;

import lombok.Value;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.sku.SKU;

@Value
public class WarehouseValidatedEntities {

    SKU sku;
    ContainerDTO containerDTO;

    FGInboundDTO fgInboundDTO;

    OutboundDTO outboundDTO;


    private WarehouseValidatedEntities(SKU sku, ContainerDTO containerDTO, FGInboundDTO fgInboundDTO, OutboundDTO outboundDTO) {
        this.sku = sku;
        this.containerDTO = containerDTO;
        this.fgInboundDTO = fgInboundDTO;
        this.outboundDTO = outboundDTO;
    }

    public static class Builder {
        SKU sku;
        ContainerDTO containerDTO;
        FGInboundDTO fgInboundDTO;
        OutboundDTO outboundDTO;

        public WarehouseValidatedEntities.Builder sku(SKU sku) {
            this.sku = sku;
            return this;
        }
        public WarehouseValidatedEntities.Builder containerDTO(ContainerDTO containerDTO) {
            this.containerDTO = containerDTO;
            return this;
        }

        public WarehouseValidatedEntities.Builder fgInboundDTO(FGInboundDTO fgInboundDTO) {
            this.fgInboundDTO = fgInboundDTO;
            return this;
        }

        public WarehouseValidatedEntities.Builder outboundDTO(OutboundDTO outboundDTO) {
            this.outboundDTO = outboundDTO;
            return this;
        }

        public WarehouseValidatedEntities build() {

            return new WarehouseValidatedEntities(this);
        }
    }

    private WarehouseValidatedEntities(WarehouseValidatedEntities.Builder b) {
        this.sku = b.sku;
        this.containerDTO = b.containerDTO;
        this.fgInboundDTO = b.fgInboundDTO;
        this.outboundDTO = b.outboundDTO;
    }

}
