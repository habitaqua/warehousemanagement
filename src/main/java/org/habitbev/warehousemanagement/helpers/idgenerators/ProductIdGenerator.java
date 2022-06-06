package org.habitbev.warehousemanagement.helpers.idgenerators;

import java.util.List;

public interface ProductIdGenerator<T> {

    List<String> generate(T input);
}


