package org.warehousemanagement.utils;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.Barcode1D;
import org.apache.commons.collections4.MapUtils;
import org.warehousemanagement.entities.BarcodeDataDTO;

import java.util.Map;
import java.util.Optional;


public class Utilities {


    public static boolean validateContainerPredefinedCapacities(Map<String, Integer> skuWiseCapacity) {
        if (MapUtils.isEmpty(skuWiseCapacity))
            return false;
        Optional<Integer> capacityOutOfBounds = skuWiseCapacity.values().stream().filter(value -> value <= 0).findAny();
        if (capacityOutOfBounds.isPresent())
            return false;
        return true;
    }


    public static Barcode128 createBarcode128(BarcodeDataDTO barcodeDataDTO) {
        Barcode128 barcode = new Barcode128(barcodeDataDTO.getPdfDocument());
        barcode.setAltText(barcodeDataDTO.getAltText());
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setCode(barcodeDataDTO.getValueToEncode());
        return barcode;
    }
}
