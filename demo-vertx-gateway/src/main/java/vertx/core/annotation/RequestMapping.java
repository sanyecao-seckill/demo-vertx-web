package vertx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: sanyecao-w
 * @Date: 2021/10/13
 * @Version: 1.0.0
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    /**
     * 适配URL
     * @return
     */
    String value() default "";

    /**
     * 响应结果类型
     * @return: ResponseType
     * See Also ResponseType.JSON ResponseType.HTML
     */
    ResponseType rt() default ResponseType.JSON;

    /**
     * 是否异步
     */
    boolean async() default false;
}
