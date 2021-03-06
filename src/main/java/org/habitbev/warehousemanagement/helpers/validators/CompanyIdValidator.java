package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.service.CompanyService;

import java.util.Optional;

public class CompanyIdValidator implements WarehouseActionEntitiesValidator {

    CompanyService companyService;

    @Inject
    public CompanyIdValidator(CompanyService companyService) {
        this.companyService = companyService;
    }


    @Override
    public WarehouseValidatedEntities.Builder validate(WarehouseActionValidationRequest input, WarehouseValidatedEntities.Builder gatheredWarehouseEntities) {
        Preconditions.checkArgument(input != null, "input cannot be null");
        Preconditions.checkArgument(gatheredWarehouseEntities != null, "gatheredWarehouseEntities cannot be null");
        String companyId = input.getCompanyId();
        Preconditions.checkArgument(StringUtils.isNotBlank(companyId), "companyId cannot be blank");
        try {
            CompanyDTO companyDTO = companyService.getCompany(companyId);
            return gatheredWarehouseEntities.companyDTO(companyDTO);
        } catch (ResourceNotAvailableException e) {
            throw new WarehouseActionValidationException(e);
        }
    }
}
