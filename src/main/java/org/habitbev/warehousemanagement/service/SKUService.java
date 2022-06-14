package org.habitbev.warehousemanagement.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.habitbev.warehousemanagement.dao.SKUDAO;
import org.habitbev.warehousemanagement.entities.exceptions.ResourceNotAvailableException;
import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;

import java.util.List;
import java.util.Optional;

public class SKUService {


    SKUDAO skudao;

    @Inject
    public SKUService(@Named("configSKUDAOImpl") SKUDAO skudao) {
        this.skudao = skudao;
    }


    public List<SKUDTO> getAll(String companyId) {
        return skudao.getAll(companyId);
    }


    public SKUDTO get(String companyId, String skuCode) {

        Optional<SKUDTO> skuOp = skudao.get(companyId, skuCode);
        if (!skuOp.isPresent()) {

            String message = String.format("Given companyId %s and skuCode %s combination does not exist", companyId, skuCode);
            throw new ResourceNotAvailableException(message);
        }
        return skuOp.get();
    }
}
