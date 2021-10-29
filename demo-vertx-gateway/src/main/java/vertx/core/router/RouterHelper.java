package vertx.core.router;

import com.alibaba.fastjson.JSON;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vertx.core.SpringContextHolder;
import vertx.core.annotation.RequestMapping;
import vertx.core.annotation.ResponseType;
import vertx.exception.BizException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * @Description: router配置
 * @Author: sanyecao-w
 * @Date: 2021/10/13
 * @Version: 1.0.0
 */
public class RouterHelper {

  Logger logger = LogManager.getLogger(RouterHelper.class);

  private Router router;

  public RouterHelper(Router router){
    this.router = router;
  }

  public void configRouter(){
    /**
     * 通过spring 管理的Bean,获取注解为@RequestMapping的类，
     * 并将url-pattern与对应的业务处理方法注册到Router中
     */
    SpringContextHolder.applicationContext.getBeansWithAnnotation(RequestMapping.class).
            values().forEach(this::register);
  }

  //过滤掉未配置@RequestMapping的方法
  private void register(Object handler) {
    String mainPathUrl = handler.getClass().getDeclaredAnnotation(RequestMapping.class).value();
    Arrays.stream(handler.getClass().getDeclaredMethods())
            .filter(method -> method.getAnnotation(RequestMapping.class) != null)
            .forEach(method -> registerMapping(handler,mainPathUrl,method));
  }

  //配置router，并在方法调用前后增加公共的业务逻辑
  private void registerMapping(Object handler,String mainPathUrl, Method method) {
    //获取目标方法的RequestMapping注解
    RequestMapping subPath = method.getAnnotation(RequestMapping.class);
    //需要注册的完整url
    String url = mainPathUrl + subPath.value();
    logger.info("Register Mapping Url is:"+url);
    //将url与对应的目标方法注册到router中（并且设置同一个Verticle实例下的handler可以并发执行，true则是按顺序执行）
    router.route(url).blockingHandler(context -> {
      //1.添加监控
      //2.添加限流
      //3.其他操作...
      try {
        //4.调用对应业务方法
        Object obj = doInvoke(context,handler,method);
        //5.1 正常响应结果
        responseNormal(context,obj,subPath.rt(),subPath.async());
      } catch (Exception e) {
        logger.error("invoke target method exception :"+url,e);
        //5.2 异常响应结果
        responseException(context,subPath.rt());
      }finally {
        //6.结束监控
        //7.其他操作
      }
    }, false);

  }

  private Object doInvoke(RoutingContext context,Object handler, Method method) throws InvocationTargetException, IllegalAccessException, BizException {
    //获取目标方法所有入参
    Parameter[] parameters = method.getParameters();
    //入参为空，则直接调用
    if(parameters==null || parameters.length==0){
      //返回目标方法的调用结果
      return method.invoke(handler);
    }
    Parameter parameter1 = parameters[0];
    //目标方法的入参类型-RoutingContext类型
    if(RoutingContext.class == parameter1.getType()){
      //返回目标方法的调用结果
      return method.invoke(handler,context);
    }
    //目标方法的入参类型-HttpServerRequest类型，正常情况下使用该类型，即可获取所有请求信息
    else if(HttpServerRequest.class == parameter1.getType()){
      //返回目标方法的调用结果
      return method.invoke(handler,context.request());
    }
    //TODO 目前目标方法的入参，只支持两种，一个是RoutingContext类型，一个是HttpServerRequest类型，更多的类型有待扩展
    throw new BizException("目标方法的入参，有暂不支持的类型，请做调整");
  }

  private void responseNormal(RoutingContext context,Object responseData, ResponseType rt,boolean async) throws BizException {
    //如果是异步结果，则不同步返回
    if(async){
      return;
    }
    //返回类型-json
    if(ResponseType.JSON == rt){
      context.response()
              .putHeader("content-type", "application/json")
              .end(JSON.toJSONString(responseData));
      return;
    }
    //返回类型-html,则内部重定向到对应的静态html
    if(!(responseData instanceof String)){
      throw new BizException("返回结果异常");
    }
    context.reroute((String)responseData);
  }

  private void responseException(RoutingContext context, ResponseType rt){
    //返回类型-json
    if(ResponseType.JSON == rt){
      context.response()
              .putHeader("content-type", "application/json")
              .end("{'msg':'内部服务错误','errorCode':'500'}");
      return;
    }
    //返回类型-html,则内部重定向到对应的静态html
    context.reroute("/html/error.html");
  }

}
