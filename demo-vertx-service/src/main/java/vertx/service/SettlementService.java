package vertx.service;


import com.demo.support.dto.SettlementOrderDTO;
import vertx.exception.BizException;
import vertx.model.SettlementInitDTO;
import vertx.model.SettlementSubmitDTO;

import java.util.Map;

public interface SettlementService {

    SettlementInitDTO initData(String productId, String buyNum) throws BizException;

    Map<String,Object> dependency();

    SettlementSubmitDTO submitOrder(SettlementOrderDTO requestDTO) throws BizException;

}
