package org.habitbev.warehousemanagement.entities;

import lombok.Value;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.customer.CustomerDTO;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;

@Value
public class WarehouseValidatedEntities {

    SKUDTO skuDTO;
    ContainerDTO containerDTO;

    FGInboundDTO fgInboundDTO;

    OutboundDTO outboundDTO;

    CompanyDTO companyDTO;

    CustomerDTO customerDTO;

    WarehouseDTO warehouseDTO;


    private WarehouseValidatedEntities(SKUDTO skuDTO, ContainerDTO containerDTO, FGInboundDTO fgInboundDTO,
                                       OutboundDTO outboundDTO, CompanyDTO companyDTO, CustomerDTO customerDTO,
                                       WarehouseDTO warehouseDTO) {
        this.skuDTO = skuDTO;
        this.containerDTO = containerDTO;
        this.fgInboundDTO = fgInboundDTO;
        this.outboundDTO = outboundDTO;
        this.companyDTO = companyDTO;
        this.customerDTO = customerDTO;
        this.warehouseDTO = warehouseDTO;
    }

    public static class Builder {
        SKUDTO skuDTO;
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

        public WarehouseValidatedEntities.Builder skuDTO(SKUDTO skuDTO) {
            this.skuDTO = skuDTO;
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
        this.skuDTO = b.skuDTO;
        this.containerDTO = b.containerDTO;
        this.fgInboundDTO = b.fgInboundDTO;
        this.outboundDTO = b.outboundDTO;
        this.companyDTO = b.companyDTO;
        this.customerDTO = b.customerDTO;
        this.warehouseDTO = b.warehouseDTO;
    }

}
