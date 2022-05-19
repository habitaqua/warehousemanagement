package org.warehousemanagement.service.location;

import com.google.inject.Inject;
import org.warehousemanagement.entities.Capacity;
import org.warehousemanagement.entities.PaginatedResponse;
import org.warehousemanagement.entities.dynamodb.Location;
import org.warehousemanagement.entities.dynamodb.LocationSKUMapping;
import org.warehousemanagement.entities.location.AddLocationRequest;
import org.warehousemanagement.entities.location.GetLocationRequest;
import org.warehousemanagement.entities.location.GetLocationsRequest;
import org.warehousemanagement.entities.location.LocationDTO;
import org.warehousemanagement.entities.location.locationstatus.Available;
import org.warehousemanagement.entities.locationskumapping.GetAllLocationSKUMappingsRequest;
import org.warehousemanagement.idGenerators.LocationIdGenerator;
import org.warehousemanagement.sao.LocationDbSAO;
import org.warehousemanagement.sao.LocationSKUMappingDbSAO;
import org.warehousemanagement.sao.WarehouseDbSAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationService {

    LocationDbSAO locationDbSAO;
    WarehouseDbSAO warehouseDbSAO;
    LocationSKUMappingDbSAO locationSKUMappingDbSAO;

    LocationIdGenerator<AddLocationRequest> locationIdGenerator;

    @Inject
    public LocationService(LocationDbSAO locationDbSAO, WarehouseDbSAO warehouseDbSAO,
                           LocationSKUMappingDbSAO locationSKUMappingDbSAO, LocationIdGenerator locationIdGenerator) {
        this.locationDbSAO = locationDbSAO;
        this.warehouseDbSAO = warehouseDbSAO;
        this.locationSKUMappingDbSAO = locationSKUMappingDbSAO;
        this.locationIdGenerator = locationIdGenerator;
    }

    public String add(AddLocationRequest addLocationRequest) {

        String warehouseId = addLocationRequest.getWarehouseId();
        synchronized (warehouseId) {
            String newLocationId = locationIdGenerator.generate(addLocationRequest);
            LocationDTO locationDTO = LocationDTO.builder().locationId(newLocationId).warehouseId(warehouseId).currentCapacity(0)
                    .totalCapacity(addLocationRequest.getTotalCapacity().getQty()).uom(addLocationRequest.getTotalCapacity().getUom())
                    .status(new Available()).build();
            locationDbSAO.add(locationDTO);
            return newLocationId;
        }
    }

    public PaginatedResponse<LocationDTO> getLocations(GetLocationsRequest getLocationsRequest) {
        PaginatedResponse<Location> locations = locationDbSAO.getLocations(getLocationsRequest);
        List<LocationDTO> locationDTOs = locations.getItems().stream().map(LocationDTO::fromLocation).collect(Collectors.toList());
        PaginatedResponse.PaginatedResponseBuilder<LocationDTO> builder = PaginatedResponse.<LocationDTO>builder().items(locationDTOs);
        if (locations.getNextPageToken().isPresent()) {
            builder.nextPageToken(locations.getNextPageToken().get()).build();
        }
        return builder.build();
    }

    public Optional<LocationDTO> getLocation(GetLocationRequest getLocationRequest) {
        Optional<Location> locationOp = locationDbSAO.getLocation(getLocationRequest);

        if (locationOp.isPresent()) {
            Location location = locationOp.get();
            GetAllLocationSKUMappingsRequest getAllLocationSKUMappingsRequest = GetAllLocationSKUMappingsRequest.builder().locationId(location.getLocationId()).warehouseId(location.getWarehouseId()).build();

            List<LocationSKUMapping> allLocationSKUMappings = locationSKUMappingDbSAO.getAllLocationSKUMappings(getAllLocationSKUMappingsRequest);
            Map<String, Capacity> skuDetailToCapacityMap = new HashMap<>();
            allLocationSKUMappings.stream().forEach(skuDetail -> skuDetailToCapacityMap
                    .put(skuDetail.getSkuCategoryAndType(), Capacity.builder().qty(skuDetail.getQuantity())
                            .uom(skuDetail.getUom()).build()));
            LocationDTO locationDTO = LocationDTO.fromLocation(location);
            locationDTO.setLocationSKUDetails(skuDetailToCapacityMap);
            return Optional.of(locationDTO);
        }
        return Optional.empty();
    }

}
