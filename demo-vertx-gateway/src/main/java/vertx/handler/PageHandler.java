package vertx.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import vertx.core.annotation.RequestMapping;
import vertx.core.annotation.ResponseType;

@Component
@RequestMapping( "/settlement" )
public class PageHandler {

    Logger logger = LogManager.getLogger(PageHandler.class);

    @RequestMapping(value = "/page", rt = ResponseType.HTML)
    public String page() {
        return "/html/settlement.html";
    }

}
