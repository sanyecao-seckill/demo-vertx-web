package vertx.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Description: 项目启动器
 * @Author: sanyecao-w
 * @Date: 2021/10/13
 * @Version: 1.0.0
 */
public class MainLauncher {

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(InitialModule.class);
    }

}
