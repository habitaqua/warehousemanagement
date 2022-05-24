package org.warehousemanagement.dao;

import org.warehousemanagement.entities.dynamodb.FinishedGoodsOutbound;
import org.warehousemanagement.entities.outbound.OutboundDTO;

import java.util.Optional;

public interface OutboundDAO {

     void add(OutboundDTO outboundDTO) ;

     void update(OutboundDTO outboundDTO);

     Optional<FinishedGoodsOutbound> getLastOutbound(String warehouseId) ;


}
