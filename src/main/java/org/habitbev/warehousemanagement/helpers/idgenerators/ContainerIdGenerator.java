package org.habitbev.warehousemanagement.helpers.idgenerators;

public interface ContainerIdGenerator<T> {

    String generate(T input);
}
