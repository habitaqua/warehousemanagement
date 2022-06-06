package org.habitbev.warehousemanagement.helpers.idgenerators;

public interface OutboundIdGenerator<T> {

    String generate(T input);
}
