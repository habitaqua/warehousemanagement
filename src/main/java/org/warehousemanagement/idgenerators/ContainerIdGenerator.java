package org.warehousemanagement.idgenerators;

public interface ContainerIdGenerator<T> {

    String generate(T input);
}
