package vertx.handler;

import com.alibaba.fastjson.JSON;
import com.demo.support.dto.SettlementOrderDTO;
import io.vertx.core.http.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vertx.core.annotation.RequestMapping;
import vertx.exception.BizException;
import vertx.limit.RateLimiterComponent;
import vertx.model.SettlementInitDTO;
import vertx.model.SettlementSubmitDTO;
import vertx.service.SettlementService;

@Component
@RequestMapping( "/settlement" )
public class SettlementHandler {

    @Autowired
    SettlementService settlementService;

    @Autowired
    RateLimiterComponent rateLimiterComponent;

    Logger logger = LogManager.getLogger(SettlementHandler.class);

    /**
     * 结算页初始化
     * @return
     */
    @RequestMapping("/initData")
    public SettlementInitDTO initData(HttpServerRequest request) {
        String productId = request.getParam("productId");
        String buyNum = request.getParam("buyNum");

        logger.info("结算页初始化入参productId:"+productId+" ;buyNum="+buyNum);

        //判断是否被限流
        if(rateLimiterComponent.isLimitedByInit()){
            return null;
        }

        SettlementInitDTO initDTO = null;
        try {
            initDTO = settlementService.initData(productId, buyNum);
        } catch (BizException e) {
            return initDTO;
        }catch (Exception e){
            logger.error("初始化结算页接口异常",e);
        }

        return initDTO;
    }

    /**
     * 其他依赖数据的接口
     * @return
     */
    @RequestMapping("/dependency")
    public String dependency() {
        //判断是否被限流
        if(rateLimiterComponent.isLimitedByInit()){
            logger.info("我被限流了！");
            return "sss";
        }
        logger.info("我通过了！");
        return "create seckill activity fail!";
    }

    /**
     * 提交订单
     * @return
     */
    @RequestMapping("/submitData")
    public SettlementSubmitDTO submitData(HttpServerRequest request){

        //判断是否被限流
        if(rateLimiterComponent.isLimitedBySubmit()){
            return null;
        }

        SettlementOrderDTO submitDTO = new SettlementOrderDTO();

        submitDTO.setProductId(request.getParam("productId"));
        submitDTO.setAddress(request.getParam("address"));
        submitDTO.setPayType(Integer.parseInt(request.getParam("payType")));
        submitDTO.setBuyNum(Integer.parseInt(request.getParam("buyNum")));
        submitDTO.setUserId("");//从cookie中解析

        SettlementSubmitDTO responseDTO = new SettlementSubmitDTO();
        responseDTO.setCode("000000");

        logger.info("结算页提单入参:"+ JSON.toJSONString(submitDTO));
        try {
            responseDTO = settlementService.submitOrder(submitDTO);
        } catch (BizException e) {
            responseDTO.setCode("100000");
            responseDTO.setCode(e.getMessage());
            return responseDTO;
        }catch (Exception e){
            responseDTO.setCode("100000");
            responseDTO.setMessage("系统出小差了，请稍后再试");
            logger.error("结算页提单入参",e);
        }

        return responseDTO;
    }

}
