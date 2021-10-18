package vertx.core.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import vertx.core.router.RouterHelper;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: sanyecao-w
 * @Date: 2021/10/13
 */
public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) {
    //创建router
    Router router = Router.router(vertx);

    //1.给router配置通用handler
    configCommonHandler(router);

    //2.配置router-业务URL与处理方法
    new RouterHelper(router).configRouter();

    //3.静态资源配置，设置启用缓存
    StaticHandler staticHandler = StaticHandler.create().setCachingEnabled(true);
    staticHandler.setWebRoot("demo-vertx-gateway/src/main/resources");

    //3.1 静态HTML，正则匹配
    router.routeWithRegex(".*\\.html").blockingHandler(staticHandler, false);

    //3.2 静态JPG，正则匹配（静态资源上到CDN后，则不再需要相关配置）
    router.routeWithRegex(".*\\.jpg").blockingHandler(staticHandler, false);

    //4.设置Http服务配置
    HttpServerOptions serverOptions = new HttpServerOptions();
    //连接超时时间（保持长连接）
    serverOptions.setIdleTimeout(20000).setIdleTimeoutUnit(TimeUnit.MILLISECONDS);

    //5.启动服务，设置监听端口号
    vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void configCommonHandler(Router router){
    /**
     * 将form表单的入参，放入Params,
     * -不放入，可以通过request.getFormAttribute("")获取
     * -放入，不管参数在URL上还是在form表单中，都可以通过request.getParam()获取
     */
    router.route().handler(BodyHandler.create());

    //TODO 统一拦截器，比如校验登录态、打印日志等

  }
}
