package org.warehousemanagement.idGenerators;

public interface InboundIdGenerator<T> {

    String generate(T input);
}
