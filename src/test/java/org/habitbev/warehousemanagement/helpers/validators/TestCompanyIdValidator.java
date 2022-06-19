package org.habitbev.warehousemanagement.helpers.validators;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.company.CompanyDTO;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;
import org.habitbev.warehousemanagement.service.CompanyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestCompanyIdValidator {

    public static final String COMPANY_1 = "company-1";
    public static final String SKU_CODE = "skuCode";
    public static final WarehouseActionValidationRequest WAREHOUSE_ACTION_VALIDATION_REQUEST = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).build();
    @Mock
    CompanyService companyService;
    CompanyIdValidator companyIdValidator;

    @Before
    public void setUp() throws Exception {
        companyIdValidator = new CompanyIdValidator(companyService);
    }

    @Test
    public void test_validate_input_null() {

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> companyIdValidator.validate(null, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("input cannot be null");
        verifyZeroInteractions(companyService);
    }

    @Test
    public void test_validate_builder_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> companyIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, null))
                .withMessageContaining("gatheredWarehouseEntities cannot be null");
        verifyZeroInteractions(companyService);
    }

    @Test
    public void test_companyId_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> companyIdValidator.validate(WAREHOUSE_ACTION_VALIDATION_REQUEST, new WarehouseValidatedEntities.Builder()))
                .withMessageContaining("companyId cannot be blank");
        verifyZeroInteractions(companyService);
    }

    @Test
    public void test_validate_success() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().companyId(COMPANY_1).warehouseAction(WarehouseAction.START_INBOUND).build();

        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        CompanyDTO validatedCompanyEntity = CompanyDTO.builder().id(input.getCompanyId()).name(input.getCompanyId()).build();
        when(companyService.getCompany(input.getCompanyId())).thenReturn(validatedCompanyEntity);
        WarehouseValidatedEntities.Builder outputActualBuilder = companyIdValidator.validate(input, inputBuilder);
        WarehouseValidatedEntities.Builder expectedBuilder = inputBuilder.companyDTO(validatedCompanyEntity);
        new ObjectAssert<>(outputActualBuilder).usingRecursiveComparison().isEqualTo(expectedBuilder);
        verify(companyService).getCompany(eq(input.getCompanyId()));
        verifyNoMoreInteractions(companyService);
    }

    @Test
    public void test_validate_resource_not_available_exception() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).companyId(COMPANY_1).build();
        CompanyDTO validatedCompanyEntity = CompanyDTO.builder().id(input.getCompanyId()).name(input.getCompanyId()).build();
        when(companyService.getCompany(input.getCompanyId())).thenThrow(new ResourceNotAvailableException("companyid doesn't exist"));
        WarehouseValidatedEntities.Builder inputBuilder = new WarehouseValidatedEntities.Builder().skuDTO(SKUDTO.builder().skuCode(SKU_CODE).build());
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(() -> companyIdValidator.validate(input, inputBuilder)).withCauseExactlyInstanceOf(ResourceNotAvailableException.class);
        verify(companyService).getCompany(eq(input.getCompanyId()));
        verifyNoMoreInteractions(companyService);
    }
}
