package org.warehousemanagement.idgenerators;

import java.util.List;

public interface ProductIdGenerator<T> {

    List<String> generate(T input);
}


