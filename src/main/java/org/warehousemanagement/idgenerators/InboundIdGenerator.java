package org.warehousemanagement.idgenerators;

public interface InboundIdGenerator<T> {

    String generate(T input);
}
