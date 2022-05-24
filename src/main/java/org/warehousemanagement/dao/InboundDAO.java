package org.warehousemanagement.dao;

import org.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.warehousemanagement.entities.inbound.FGInboundDTO;

import java.util.Optional;

public interface InboundDAO {

    void add(FGInboundDTO fgInboundDTO);

    void update(FGInboundDTO fgInboundDTO);

    Optional<FinishedGoodsInbound> getLastInbound(String warehouseId);


}
