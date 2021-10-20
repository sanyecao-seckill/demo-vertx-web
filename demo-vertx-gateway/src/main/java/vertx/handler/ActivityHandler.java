package vertx.handler;


import com.demo.support.dto.Result;
import com.demo.support.dto.SeckillActivityDTO;
import com.demo.support.export.ActivityExportService;
import io.vertx.core.http.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vertx.cache.ILocalCache;
import vertx.core.annotation.RequestMapping;
import vertx.model.ActivityDetailDTO;

import javax.annotation.Resource;
import java.util.Date;

@Component
@RequestMapping( "/activity" )
public class ActivityHandler {

    @Autowired
    ActivityExportService activityExportService;

    @Resource(name = "activityLocalCache")
    ILocalCache iLocalCache;

    Logger logger = LogManager.getLogger(ActivityHandler.class);

    /**
     * 查询活动库存
     * @return
     */
    //    @CrossOrigin
    @RequestMapping(value = "/queryStore")
    public Integer queryStore(HttpServerRequest request) {
        String productId = request.getParam("productId");
        try{
            Result<Integer> result = activityExportService.queryStore(productId);
            return result.getData();
        }catch (Exception e){
            logger.error("query activity store exception:",e);
            return null;
        }
    }


    /**
     * 查询活动信息
     * @return
     */
//    @CrossOrigin
    @RequestMapping("/subQuery")
    public ActivityDetailDTO subQuery(HttpServerRequest request) {
        String productId = request.getParam("productId");

        logger.info("productId:"+productId);


        SeckillActivityDTO activityDTO = (SeckillActivityDTO)iLocalCache.get(productId);
        if(activityDTO == null){
            return null;
        }

        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setProductPrice(activityDTO.getActivityPrice().toPlainString());
        detailDTO.setProductPictureUrl(activityDTO.getActivityPictureUrl());
        detailDTO.setProductName(activityDTO.getActivityName());

        Integer isAvailable = 1;
        if(activityDTO.getStockNum()<=0){
            isAvailable = 0;
        }
        Date now = new Date();
        if(now.before(activityDTO.getActivityStart()) || now.after(activityDTO.getActivityEnd())){
            isAvailable = 0;
        }
        detailDTO.setIsAvailable(isAvailable);

        return detailDTO;

    }

}
