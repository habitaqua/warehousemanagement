package org.habitbev.warehousemanagement.service;

import org.habitbev.warehousemanagement.entities.sku.SKU;

import java.util.List;

public interface SKUService {

     List<SKU> getAll(String companyId);
     SKU get(String companyId, String skuCode);
}
