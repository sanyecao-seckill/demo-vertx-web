package vertx.core;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.*;
import vertx.core.verticle.*;

/**
 * @Description:
 * @Author: sanyecao-w
 * @Date: 2021/10/13
 * @Version: 1.0.0
 */
@Configuration
@ImportResource("classpath:spring/spring-config.xml")
@ComponentScan("vertx")
public class InitialModule {

  Logger logger = LogManager.getLogger(InitialModule.class);

  /**
   *
   * DependsOn :依赖 springContextHolder 先实例化完成，其 requestMappingContext才会有值
   */
  @Bean
  @DependsOn({"springContextHolder"})
  public DeploymentOptions deployVerticle(VertxOptions vertxOptions,DeploymentOptions deploymentOptions) {
    //初始化Vertx
    Vertx vertx = Vertx.vertx(vertxOptions);
    //部署实例
    vertx.deployVerticle(MainVerticle.class.getName(), deploymentOptions, stringAsyncResult -> {
      if(stringAsyncResult.succeeded()){
        logger.info("MainVerticle is deployed success!");
      }else{
        logger.error("MainVerticle is deployed Fail.. :"+stringAsyncResult.cause().toString());
      }
    });
    return deploymentOptions;
  }

}
