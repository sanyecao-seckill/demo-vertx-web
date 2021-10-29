package vertx.handler;

import com.demo.support.dto.Result;
import com.demo.support.dto.SeckillActivityDTO;
import com.demo.support.export.ActivityExportService;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vertx.core.annotation.RequestMapping;
import vertx.core.annotation.ResponseType;

import java.util.function.Function;

@Component
@RequestMapping("/hello")
public class Test2Handler {

    Logger logger = LogManager.getLogger(Test2Handler.class);

    @Autowired
    ActivityExportService activityExportService;

    @RequestMapping("/testJson")
    public String testJson(RoutingContext context) {
        return "hello world!!!"+context.request().getParam("name");
    }

    @RequestMapping(value="/testHtml.action",rt= ResponseType.HTML)
    public String testHtml() {
        return "/html/test.html";
    }

    @RequestMapping(value="/testBasic")
    public String testBasic(HttpServerRequest request) {
        return "sss";
    }

    /**
     * 测试vertx的异步功能
     * @param context
     */
    @RequestMapping(value="/testAsync",async = true)
    public void testAsync(RoutingContext context){
        Future<String> future = context.vertx().executeBlocking(new Handler<Promise<String>>() {
            //运行在worker pool中
            @Override
            public void handle(Promise<String> promise) {
                String productId = context.request().getParam("productId");
                logger.info("future executeBlocking in "+Thread.currentThread().getName());
                Result<SeckillActivityDTO> activityDTOResult = activityExportService.queryActivity(productId);
                logger.info("future executeBlocking in "+Thread.currentThread().getName()+";state:"+Thread.currentThread().getState());
                promise.complete(productId);
            }
        });

        //任务完成时执行
        future.onComplete(new Handler<AsyncResult<String>>() {
            //运行在eventloop pool中，根据golden rule，不能运行阻塞代码和执行时间较长代码
            @Override
            public void handle(AsyncResult<String> stringAsyncResult) {
                logger.info("future complete execute in "+Thread.currentThread().getName());
                String futureResult = stringAsyncResult.result();
                logger.info("future result is :"+futureResult);
                context.response().putHeader("content-type", "application/json").end(futureResult);
            }
        });
    }

    /**
     * future2 依赖future1的返回结果
     * @param context
     */
    private void test2( RoutingContext context) {
        Future<Integer> future1 = context.vertx().executeBlocking(new Handler<Promise<Integer>>() {
            //运行在worker pool中
            @Override
            public void handle(Promise<Integer> promise) {
                System.out.println("future executeBlocking in " + Thread.currentThread().getName());
                promise.complete(1);
            }
        });

        //如果接下来的代码，依赖future的返回结果,则在future successed后执行; .eventually() 不管future结果，始终会被执行
        Future<String> future2 = future1.compose(new Function<Integer, Future<String>>() {
            /** 在vert.x-eventloop-thread
             * @param s future1的执行结果
             * @return Future<Integer>
             */
            @Override
            public Future<String> apply(Integer s) {
                return context.vertx().executeBlocking(new Handler<Promise<String>>() {
                    //运行在worker pool中
                    @Override
                    public void handle(Promise<String> promise) {
                        System.out.println("future2 executeBlocking2 in " + Thread.currentThread().getName());
                        promise.complete("222" + "-" + s.toString());
                    }
                });

            }
        });

        future2.onComplete(new Handler<AsyncResult<String>>() {
            //运行在eventloop pool中，根据golden rule，不能运行阻塞代码和执行时间较长代码
            @Override
            public void handle(AsyncResult<String> stringAsyncResult) {
                System.out.println("future2 complete execute in "+Thread.currentThread().getName());
                String futureResult = stringAsyncResult.result();
                System.out.println("future2 result is :"+futureResult);
                context.response().putHeader("content-type", "application/json").end(futureResult);
            }
        });
    }

    /**
     * 依赖两个异步future的返回结果
     * @param context
     */
    private void test3( RoutingContext context) {
        Future<Integer> future1 = context.vertx().executeBlocking(new Handler<Promise<Integer>>() {
            //运行在worker pool中
            @Override
            public void handle(Promise<Integer> promise) {
                System.out.println("future executeBlocking in " + Thread.currentThread().getName());
                promise.complete(1);
            }
        });

        Future<Integer> future2 = context.vertx().executeBlocking(new Handler<Promise<Integer>>() {
            //运行在worker pool中
            @Override
            public void handle(Promise<Integer> promise) {
                System.out.println("future2 executeBlocking in " + Thread.currentThread().getName());
                promise.complete(2);
            }
        });

        CompositeFuture.all(future1,future2).onComplete(new Handler<AsyncResult<CompositeFuture>>() {
            @Override
            public void handle(AsyncResult<CompositeFuture> compositeFutureAsyncResult) {
                //future1.result() 和 compositeFutureAsyncResult.result().resultAt(0)两种方式都可用
//        future1.result();
//        future2.result();
                System.out.println("future1 结果："+future1.result());
//        compositeFutureAsyncResult.result().list();
                System.out.println("两个future执行结果："+ compositeFutureAsyncResult.result().resultAt(0));
            }
        });

    }
}
