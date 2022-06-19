package org.habitbev.warehousemanagement.helpers.validators;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.habitbev.warehousemanagement.entities.WarehouseValidatedEntities;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.container.containerstatus.PartiallyFilled;
import org.habitbev.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.habitbev.warehousemanagement.entities.exceptions.InconsistentStateException;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.exceptions.WarehouseActionValidationException;
import org.habitbev.warehousemanagement.entities.inventory.WarehouseActionValidationRequest;
import org.habitbev.warehousemanagement.entities.warehouse.WarehouseDTO;
import org.habitbev.warehousemanagement.service.ContainerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestContainerForOutboundValidator {

    public static final String WAREHOUSE_1 = "warehouse-1";
    public static final int CAPACITY_TO_OUTBOUND = 1;
    public static final String SKU_CODE = "skuCode";
    public static final String CONTAINER_1 = "container-1";
    public static final String COMPANY_1 = "company-1";
    public static final long EPOCH_MILLI = Instant.now().toEpochMilli();
    @Mock
    ContainerService containerService;
    ContainerForOutboundValidator containerForOutboundValidator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        containerForOutboundValidator = new ContainerForOutboundValidator(containerService);
    }

    @Test
    public void test_validate_input_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(null, new WarehouseValidatedEntities.Builder())).withMessageContaining("inboundIdExistenceValidator.input cannot be null");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_builder_null() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).build(), null)).withMessageContaining("warehouseEntityBuilder cannot be null");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_containerId_null() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).warehouseId(WAREHOUSE_1).capacityToInbound(CAPACITY_TO_OUTBOUND).skuCode(SKU_CODE).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, new WarehouseValidatedEntities.Builder())).withMessageContaining("containerId cannot be blank");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_warehouseId_null() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).containerId(CONTAINER_1).capacityToInbound(CAPACITY_TO_OUTBOUND).skuCode(SKU_CODE).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, new WarehouseValidatedEntities.Builder())).withMessageContaining("warehouseId cannot be blank");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_skuCode_null() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).containerId(CONTAINER_1).capacityToInbound(CAPACITY_TO_OUTBOUND).warehouseId(WAREHOUSE_1).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, new WarehouseValidatedEntities.Builder())).withMessageContaining("skuCode cannot be blank");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_capacityToInbound_null() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().warehouseAction(WarehouseAction.START_INBOUND).containerId(CONTAINER_1).skuCode(SKU_CODE).warehouseId(WAREHOUSE_1).build();
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, new WarehouseValidatedEntities.Builder())).withMessageContaining("capacityToOutbound cannot be < 0 or null");
        verifyZeroInteractions(containerService);
    }

    @Test
    public void test_validate_success() {

        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).warehouseAction(WarehouseAction.START_INBOUND)
                .capacityToOutbound(CAPACITY_TO_OUTBOUND).skuCode(SKU_CODE).build();
        WarehouseValidatedEntities.Builder warehouseEntityBuilderInput = new WarehouseValidatedEntities.Builder().warehouseDTO(WarehouseDTO.builder()
                .id(WAREHOUSE_1).companyId(COMPANY_1).build());
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).predefinedCapacity(ImmutableMap.of(SKU_CODE, 10)).warehouseId(WAREHOUSE_1).currentCapacityDetails(ContainerCapacity.builder()
                .currentCapacity(2).warehouseContainerId(CONTAINER_1).containerStatus(new PartiallyFilled()).modifiedTime(EPOCH_MILLI).creationTime(EPOCH_MILLI).build()).build();
        GetContainerRequest getContainerRequest = GetContainerRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).build();
        when(containerService.getContainer(eq(getContainerRequest))).thenReturn(containerDTO);
        WarehouseValidatedEntities.Builder actualBuilderOutput = containerForOutboundValidator.validate(input, warehouseEntityBuilderInput);
        WarehouseValidatedEntities.Builder expectedBuilderOutput = warehouseEntityBuilderInput.containerDTO(containerDTO);
        new ObjectAssert<>(expectedBuilderOutput).usingRecursiveComparison().isEqualTo(actualBuilderOutput);
        verify(containerService).getContainer(eq(getContainerRequest));
        verifyNoMoreInteractions(containerService);
    }

    @Test
    public void test_validate_inbounding_more_than_max_capacity_exception() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).warehouseAction(WarehouseAction.START_INBOUND)
                .capacityToOutbound(9).skuCode(SKU_CODE).build();
        WarehouseValidatedEntities.Builder warehouseEntityBuilderInput = new WarehouseValidatedEntities.Builder().warehouseDTO(WarehouseDTO.builder()
                .id(WAREHOUSE_1).companyId(COMPANY_1).build());
        ContainerDTO containerDTO = new ContainerDTO.Builder().containerId(CONTAINER_1).predefinedCapacity(ImmutableMap.of(SKU_CODE, 10)).warehouseId(WAREHOUSE_1).currentCapacityDetails(ContainerCapacity.builder()
                .currentCapacity(2).warehouseContainerId(CONTAINER_1).containerStatus(new PartiallyFilled()).modifiedTime(EPOCH_MILLI).creationTime(EPOCH_MILLI).build()).build();
        GetContainerRequest getContainerRequest = GetContainerRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).build();
        when(containerService.getContainer(eq(getContainerRequest))).thenReturn(containerDTO);
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, warehouseEntityBuilderInput));
        verify(containerService).getContainer(eq(getContainerRequest));
        verifyNoMoreInteractions(containerService);
    }

    @Test
    public void test_validate_resource_not_available_exception() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).warehouseAction(WarehouseAction.START_INBOUND)
                .capacityToOutbound(9).skuCode(SKU_CODE).build();
        WarehouseValidatedEntities.Builder warehouseEntityBuilderInput = new WarehouseValidatedEntities.Builder().warehouseDTO(WarehouseDTO.builder()
                .id(WAREHOUSE_1).companyId(COMPANY_1).build());
            GetContainerRequest getContainerRequest = GetContainerRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).build();
        when(containerService.getContainer(eq(getContainerRequest))).thenThrow(new ResourceNotAvailableException("container not found"));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, warehouseEntityBuilderInput))
                .withCauseExactlyInstanceOf(ResourceNotAvailableException.class);
        verify(containerService).getContainer(eq(getContainerRequest));
        verifyNoMoreInteractions(containerService);
    }

    @Test
    public void test_validate_inconsistent_state_exception() {
        WarehouseActionValidationRequest input = WarehouseActionValidationRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).warehouseAction(WarehouseAction.START_INBOUND)
                .capacityToOutbound(9).skuCode(SKU_CODE).build();
        WarehouseValidatedEntities.Builder warehouseEntityBuilderInput = new WarehouseValidatedEntities.Builder().warehouseDTO(WarehouseDTO.builder()
                .id(WAREHOUSE_1).companyId(COMPANY_1).build());
        GetContainerRequest getContainerRequest = GetContainerRequest.builder().containerId(CONTAINER_1).warehouseId(WAREHOUSE_1).build();
        when(containerService.getContainer(eq(getContainerRequest))).thenThrow(new InconsistentStateException("container inconsistent"));
        Assertions.assertThatExceptionOfType(WarehouseActionValidationException.class).isThrownBy(() -> containerForOutboundValidator.validate(input, warehouseEntityBuilderInput))
                .withCauseExactlyInstanceOf(InconsistentStateException.class);
        verify(containerService).getContainer(eq(getContainerRequest));
        verifyNoMoreInteractions(containerService);
    }
}
