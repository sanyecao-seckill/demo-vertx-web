package vertx.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Test4Handler implements Handler<RoutingContext> {

    Logger logger = LogManager.getLogger(Test4Handler.class);

    @Override
    public void handle(RoutingContext context) {
        logger.info("test4:"+Thread.currentThread().getName());
        context.next();
    }
}
