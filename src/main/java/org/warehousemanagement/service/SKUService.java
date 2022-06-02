package org.warehousemanagement.service;

import org.warehousemanagement.entities.sku.SKU;

public interface SKUService {


     SKU get(String companyId, String skuCode);
}
