package com.qasystem;

// 导入MyBatis的Mapper扫描注解，用于自动扫描Mapper接口
import org.mybatis.spring.annotation.MapperScan;
// 导入Spring Boot的启动类
import org.springframework.boot.SpringApplication;
// 导入Spring Boot的自动配置注解
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 师生答疑系统 - Spring Boot 应用程序主启动类
 * 
 * 这是整个后端应用的入口，Spring Boot会从这里开始启动
 * 就像一本书的目录页，告诉程序从哪里开始运行
 * 
 * 功能说明：
 * 1. 启动Spring Boot应用程序
 * 2. 自动扫描和加载所有的组件（Controller、Service、Mapper等）
 * 3. 初始化数据库连接
 * 4. 配置Web服务器（默认使用Tomcat）
 * 5. 启动后会在控制台打印欢迎信息
 * 
 * @author QA System Team
 * @version 2.0.0
 * @since 2024-01-01
 */
// @SpringBootApplication：这是Spring Boot的核心注解，它包含了三个重要的功能：
// 1. @Configuration：告诉Spring这是一个配置类
// 2. @EnableAutoConfiguration：自动配置Spring应用（比如自动配置数据库、Web服务器等）
// 3. @ComponentScan：自动扫描当前包及子包下的所有组件（@Controller、@Service、@Repository等）
@SpringBootApplication
// @MapperScan：告诉MyBatis去哪里找数据库操作的Mapper接口
// "com.qasystem.mapper"：指定Mapper接口所在的包路径
// 这样就不用在每个Mapper接口上加@Mapper注解了
@MapperScan("com.qasystem.mapper")
public class QaSystemApplication {

    /**
     * 主函数 - 程序的入口点
     * 
     * Java程序运行时，JVM（Java虚拟机）会首先找到main方法并执行
     * 就像按下电脑的开机键，系统开始启动
     * 
     * @param args 命令行参数数组，可以在启动时传入参数（比如：java -jar app.jar --port=8080）
     *             在实际运行中，我们通常用配置文件而不是命令行参数
     */
    public static void main(String[] args) {
        // SpringApplication.run()：这是Spring Boot启动的核心方法
        // 参数1：QaSystemApplication.class - 告诉Spring从哪个类开始启动
        // 参数2：args - 传入命令行参数
        // 
        // 这个方法会做很多事情：
        // 1. 创建Spring容器（ApplicationContext）- 就像建立一个大仓库，存放所有的组件
        // 2. 扫描并注册所有的Bean（Controller、Service等）- 把所有零件放进仓库
        // 3. 自动配置（数据库连接、Web服务器等）- 组装这些零件
        // 4. 启动内置的Tomcat服务器 - 让应用可以接收网络请求
        SpringApplication.run(QaSystemApplication.class, args);
        
        // 在控制台打印启动成功的信息
        // 使用三引号(""")可以方便地编写多行文本（Java 13+的特性）
        System.out.println("""
            
            ========================================
            师生答疑系统 v2.0 启动成功！
            ========================================
            """);
    }
}

