package org.habitbev.warehousemanagement.service;

import org.habitbev.warehousemanagement.entities.sku.SKU;

public interface SKUService {


     SKU get(String companyId, String skuCode);
}
