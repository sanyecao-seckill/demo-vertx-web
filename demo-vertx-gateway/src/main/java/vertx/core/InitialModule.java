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
   * 设置eventloop线程池大小
   * info:建议设置cpu核数*2,但是最好需要小于等于verticle的实例数,
   *     一个verticle实例会被分配到固定的一个线程中运行
   *     一个eventloop线程，可以运行多个verticle实例
   * DependsOn :依赖 springContextHolder 先实例化完成，其 requestMappingContext才会有值
   */
  @Bean
  @DependsOn({"springContextHolder"})
  public DeploymentOptions deployVerticle(VertxOptions vertxOptions,DeploymentOptions deploymentOptions) {

    Vertx vertx = Vertx.vertx(vertxOptions);

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
