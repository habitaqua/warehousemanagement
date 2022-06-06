package org.warehousemanagement.helpers.idgenerators;

public interface InboundIdGenerator<T> {

    String generate(T input);
}
