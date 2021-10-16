package vertx.handler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.springframework.stereotype.Component;
import vertx.core.annotation.RequestMapping;
import vertx.core.annotation.ResponseType;

@Component
@RequestMapping("/hello")
public class Test2Handler {

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
}
