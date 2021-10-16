package vertx.handler;


import com.demo.support.constant.ResultCodeConstant;
import com.demo.support.dto.ProductInfoDTO;
import com.demo.support.dto.Result;
import com.demo.support.dto.SeckillActivityDTO;
import com.demo.support.export.ActivityExportService;
import com.demo.support.export.ProductExportService;
import io.vertx.core.http.HttpServerRequest;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vertx.core.annotation.RequestMapping;
import vertx.core.annotation.ResponseType;
import vertx.handler.model.ActivityDescDTO;
import vertx.handler.model.ProductDescDTO;
import vertx.model.ProductDetailDTO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
@RequestMapping( "/mock" )
public class MockHandler {

    @Autowired
    ProductExportService productExportService;

    @Autowired
    ActivityExportService activityExportService;

//    @Autowired
//    RedisTools redisTools;

    Logger logger = LogManager.getLogger(MockHandler.class);

    @RequestMapping("/createActivity")
    public String createActivity(HttpServerRequest request) {

        SeckillActivityDTO activityDTO = new SeckillActivityDTO();

        try{
            String productId = request.getParam("productId");

            String limitNum= request.getParam("limitNum");
            String activityName= request.getParam("activityName");
            String stockNum= request.getParam("stockNum");
            String activityPrice= request.getParam("activityPrice");
            String activityPictureUrl= request.getParam("activityPictureUrl");

            if(StringUtils.isBlank(limitNum)){
                limitNum = "2";
            }
            if(StringUtils.isBlank(activityName)){
                activityName = "荣耀手机特价998，性价比高，最优的选择，不再犹豫，买到即赚到";
            }
            if(StringUtils.isBlank(stockNum)){
                stockNum = "4";
            }
            if(StringUtils.isBlank(activityPrice)){
                activityPrice = "998";
            }
            if(StringUtils.isBlank(activityPictureUrl)){
                activityPictureUrl = "/images/product_seckill.jpg";
            }

            //设置基本信息
            activityDTO.setLimitNum(Integer.parseInt(limitNum));
            activityDTO.setActivityName(activityName);
            activityDTO.setStockNum(Integer.parseInt(stockNum));
            activityDTO.setActivityPrice(new BigDecimal(activityPrice));
            activityDTO.setActivityPictureUrl(activityPictureUrl);
            activityDTO.setProductId(productId);

            //设置默认开始结束时间
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH,2);
            activityDTO.setActivityStart(now);
            activityDTO.setActivityEnd(calendar.getTime());

            Result<Integer> result = activityExportService.createActivity(activityDTO);
            if(StringUtils.isEquals(result.getCode(), ResultCodeConstant.SUCCESS)){
                return "创建活动成功!";
            }else{
                return result.getMessage();
            }
        }catch (Exception e){
            logger.error(e);
        }
        return "创建活动失败!";
    }


    @RequestMapping("/activityDescData")
    public ActivityDescDTO activityDescData(HttpServerRequest request) {
        try{
//            request.getFormAttribute("")//从form表单中获取数据
            String productId = request.getParam("productId");
            Result<SeckillActivityDTO> result = activityExportService.queryActivityByCondition(productId,null);
            if(result == null || result.getData() == null){
                return null;
            }
            ActivityDescDTO descDTO = new ActivityDescDTO();
            SeckillActivityDTO activityDTO = result.getData();
            BeanUtils.copyProperties(activityDTO,descDTO);
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            descDTO.setActivityStartStr(sf.format(activityDTO.getActivityStart()));
            descDTO.setActivityEndStr(sf.format(activityDTO.getActivityEnd()));

            Integer status = activityDTO.getStatus();
            String statusDesc="";
            if(status==0){
                statusDesc="未开始";
            }else if(status==1){
                statusDesc="进行中";
            }else {
                statusDesc="已结束";
            }
            descDTO.setStatusStr(statusDesc);

            return descDTO;
        }catch (Exception e){
            logger.error("查询活动信息异常",e);
        }

        return null;
    }

    @RequestMapping("/productDescData")
    public ProductDescDTO productDescData(HttpServerRequest request) {
        try{
            String productId = request.getParam("productId");
            Result<ProductInfoDTO> productInfoDTOResult = productExportService.queryProduct(productId);

            if(productInfoDTOResult == null || productInfoDTOResult.getData() == null){
                return null;
            }

            ProductDescDTO descDTO = new ProductDescDTO();
            ProductInfoDTO productInfo = productInfoDTOResult.getData();
            BeanUtils.copyProperties(productInfo,descDTO);
            descDTO.setTagStr(productInfo.getTag()==1?"普通商品":"秒杀商品");

            return descDTO;
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    @RequestMapping("/startActivity")
    public String startActivity(HttpServerRequest request) {
        try{
            String productId = request.getParam("productId");
            Result<Integer> countResult = activityExportService.startActivity(productId);
            if(countResult==null ||countResult.getData()==null){
                return "开始秒杀活动失败！";
            }
            if(!org.springframework.util.StringUtils.endsWithIgnoreCase(countResult.getCode(), ResultCodeConstant.SUCCESS)){
                return countResult.getMessage();
            }
            if(countResult.getData() == 0){
                return "开始秒杀活动失败！";
            }
            return "开始秒杀活动成功！";
        }catch (Exception e){
            logger.error(e);
            return "开始秒杀活动失败！";
        }
    }

    @RequestMapping("/endActivity")
    public String endActivity(HttpServerRequest request) {
        try{
            String productId = request.getParam("productId");
            Result<Integer> countResult = activityExportService.endActivity(productId);
            if(countResult==null ||countResult.getData()==null){
                return "结束秒杀活动失败！";
            }
            if(!org.springframework.util.StringUtils.endsWithIgnoreCase(countResult.getCode(), ResultCodeConstant.SUCCESS)){
                return countResult.getMessage();
            }
            if(countResult.getData() == 0){
                return "结束秒杀活动失败！";
            }
            return "结束秒杀活动成功！";
        }catch (Exception e){
            logger.error(e);
            return "结束秒杀活动失败！";
        }
    }

    /**
     * 商品详情页
     * @return
     */
    @RequestMapping("/product")
    public ProductDetailDTO product(HttpServerRequest request) {
        String productId = request.getParam("productId");
        Result<ProductInfoDTO> productInfoDTOResult = productExportService.queryProduct(productId);

        if(productInfoDTOResult == null || productInfoDTOResult.getData() == null){
            return null;
        }

        ProductDetailDTO detailDTO = new ProductDetailDTO();

        ProductInfoDTO productInfo = productInfoDTOResult.getData();
        //标识  1：正常商品，2：秒杀商品 3：预约商品
        detailDTO.setProductPrice(productInfo.getProductPrice().toPlainString());
        detailDTO.setProductPictureUrl(productInfo.getPictureUrl());
        detailDTO.setIsAvailable(0);//不可购买
        detailDTO.setProductName(productInfo.getProductName());
        detailDTO.setTag(productInfo.getTag());
        return detailDTO;
    }


    @RequestMapping(value = "/activityDesc",rt = ResponseType.HTML)
    public String activityDesc() {
        return "/html/activity_desc.html";
    }

    @RequestMapping(value = "/productDesc",rt = ResponseType.HTML)
    public String productDesc() {
        return "/html/product_desc.html";
    }

    @RequestMapping(value = "/index",rt = ResponseType.HTML)
    public String index() {
        return "/html/product.html";
    }

    @RequestMapping(value = "/payPage",rt = ResponseType.HTML)
    public String payPage() {
        return "/html/payment.html";
    }

}
