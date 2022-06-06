package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.PaginatedResponse;
import org.habitbev.warehousemanagement.entities.container.ContainerDTO;
import org.habitbev.warehousemanagement.entities.container.GetContainerRequest;
import org.habitbev.warehousemanagement.entities.container.GetContainersRequest;

import java.util.Optional;

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

    Optional<ContainerDTO> getLastAddedContainer(String warehouseId);

}
