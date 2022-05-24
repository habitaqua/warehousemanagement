package org.warehousemanagement.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.warehousemanagement.entities.PaginatedResponse;
import org.warehousemanagement.entities.container.AddContainerRequest;
import org.warehousemanagement.entities.container.ContainerDTO;
import org.warehousemanagement.entities.container.GetContainerRequest;
import org.warehousemanagement.entities.container.GetContainersRequest;
import org.warehousemanagement.entities.dynamodb.Container;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.exceptions.NonRetriableException;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is the DAO for Container Table. Move to master.
 *
 * @author Laasya
 */

public interface ContainerDAO {

    Optional<ContainerDTO> getContainer(GetContainerRequest getContainerRequest);

    /**
     * gets container details without current capacity
     *
     * @param getContainersRequest
     * @return
     */
    PaginatedResponse<ContainerDTO> getContainers(GetContainersRequest getContainersRequest);


    /**
     * creates new location at the given warehouse id.
     * location id at context of a warehouse is auto incremented.
     * At a warehouse level addition of location is synchronized . This helps in keeping location id counter incremental
     *
     * @return
     */
    void add(ContainerDTO containerDTO);

    Optional<Container> getLastAddedContainer(String warehouseId);

}
