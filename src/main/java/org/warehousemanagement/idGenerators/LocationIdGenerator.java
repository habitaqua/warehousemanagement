package org.warehousemanagement.idGenerators;

public interface LocationIdGenerator<T> {

    String generate(T input);
}
