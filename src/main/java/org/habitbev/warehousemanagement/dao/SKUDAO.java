package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.sku.SKU;
import org.habitbev.warehousemanagement.entities.sku.SKUDTO;

import java.util.List;
import java.util.Optional;

public interface SKUDAO {

     List<SKUDTO> getAll(String companyId);
     Optional<SKUDTO> get(String companyId, String skuCode);
}
