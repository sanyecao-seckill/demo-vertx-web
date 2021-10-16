package vertx.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test3Handler implements Handler<RoutingContext> {
    Logger logger = LogManager.getLogger(Test3Handler.class);
    @Override
    public void handle(RoutingContext context) {
        logger.info("test3:"+Thread.currentThread().getName());
        context.next();
    }
}
