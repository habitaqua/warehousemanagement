package org.warehousemanagement.idgenerators;

public interface OutboundIdGenerator<T> {

    String generate(T input);
}
