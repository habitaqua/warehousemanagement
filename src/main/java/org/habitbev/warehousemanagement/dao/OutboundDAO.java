package org.habitbev.warehousemanagement.dao;

import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsInbound;
import org.habitbev.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.habitbev.warehousemanagement.entities.outbound.OutboundDTO;

import java.util.Optional;

public interface OutboundDAO {

     Optional<FinishedGoodsOutbound>  get(String warehouseId, String outboundId);

     void add(OutboundDTO outboundDTO) ;

     void update(OutboundDTO outboundDTO);

     Optional<FinishedGoodsOutbound> getLastOutbound(String warehouseId) ;


}
