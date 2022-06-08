package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.sku.SKU;

import java.util.List;
import java.util.Optional;

public interface SKUDAO {

     List<SKU> getAll(String companyId);
     Optional<SKU> get(String companyId, String skuCode);
}
