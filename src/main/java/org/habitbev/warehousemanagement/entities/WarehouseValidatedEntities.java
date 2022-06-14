package org.habitbev.warehousemanagement.entities;

import lombok.Value;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;

@Value
public class WarehouseValidatedEntities {

    SKU sku;
    ContainerDTO containerDTO;

    FGInboundDTO fgInboundDTO;

    OutboundDTO outboundDTO;

    CompanyDTO companyDTO;

    CustomerDTO customerDTO;

    WarehouseDTO warehouseDTO;


    private WarehouseValidatedEntities(SKU sku, ContainerDTO containerDTO, FGInboundDTO fgInboundDTO,
                                       OutboundDTO outboundDTO, CompanyDTO companyDTO, CustomerDTO customerDTO,
                                       WarehouseDTO warehouseDTO) {
        this.sku = sku;
        this.containerDTO = containerDTO;
        this.fgInboundDTO = fgInboundDTO;
        this.outboundDTO = outboundDTO;
        this.companyDTO = companyDTO;
        this.customerDTO = customerDTO;
        this.warehouseDTO = warehouseDTO;
    }

    public static class Builder {
        SKU sku;
        ContainerDTO containerDTO;
        FGInboundDTO fgInboundDTO;
        OutboundDTO outboundDTO;
        CompanyDTO companyDTO;
        CustomerDTO customerDTO;
        WarehouseDTO warehouseDTO;


        public WarehouseValidatedEntities.Builder companyDTO(CompanyDTO companyDTO) {
            this.companyDTO = companyDTO;
            return this;
        }

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

        public WarehouseValidatedEntities.Builder customerDTO(CustomerDTO customerDTO) {
            this.customerDTO = customerDTO;
            return this;
        }

        public WarehouseValidatedEntities.Builder warehouseDTO(WarehouseDTO warehouseDTO) {
            this.warehouseDTO = warehouseDTO;
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
        this.companyDTO = b.companyDTO;
        this.customerDTO = b.customerDTO;
        this.warehouseDTO = b.warehouseDTO;
    }

}
