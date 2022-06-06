package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.inbound.FGInboundDTO;

import java.util.Optional;

public interface InboundDAO {

    void add(FGInboundDTO fgInboundDTO);

    void update(FGInboundDTO fgInboundDTO);

    Optional<FinishedGoodsInbound> getLastInbound(String warehouseId);


}
