package org.warehousemanagement.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.jcabi.aspects.RetryOnFailure;
import org.apache.commons.lang3.StringUtils;
import org.warehousemanagement.dao.ContainerCapacityDAO;
import org.warehousemanagement.entities.dynamodb.ContainerCapacity;
import org.warehousemanagement.entities.exceptions.RetriableException;

import java.util.Optional;

public class ContainerCapacityService {

    ContainerCapacityDAO containerCapacityDAO;

    @Inject
    public ContainerCapacityService(ContainerCapacityDAO containerCapacityDAO) {
        this.containerCapacityDAO = containerCapacityDAO;
    }

    public Optional<ContainerCapacity> get(String warehouseId, String containerId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        return containerCapacityDAO.get(warehouseId, containerId);
    }


    public void initialize(String warehouseId, String containerId) {
        Preconditions.checkArgument(StringUtils.isNotBlank(warehouseId), "warehouseId cannot be blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(containerId), "containerId cannot be blank");
        containerCapacityDAO.init(warehouseId, containerId);
    }


}
