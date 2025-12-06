package com.qasystem.config;

// 从配置文件中读取属性值的注解
import org.springframework.beans.factory.annotation.Value;
// 标记这是一个配置类
import org.springframework.context.annotation.Configuration;
// 静态资源处理器注册表
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// Spring MVC配置接口
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 用于处理文件路径
import java.io.File;

/**
 * Web配置类 - 配置Spring MVC的Web相关设置
 * 
 * 主要功能：
 * 1. 配置静态资源访问路径（比如图片、文件）
 * 2. 让用户上传的文件可以通过URL直接访问
 * 
 * 举例说明：
 * 用户上传了一张图片到服务器的 D:/uploads/avatar.jpg
 * 通过这个配置，前端可以用 http://localhost:8080/uploads/avatar.jpg 来访问这张图片
 * 
 * 就像给文件柜编号，让别人能通过编号快速找到文件
 * 
 * @author QA System Team
 * @version 1.0
 */
// @Configuration：告诉Spring这是一个配置类，会被Spring容器管理
@Configuration
// implements WebMvcConfigurer：实现Spring MVC的配置接口
// 这个接口提供了很多Web配置的方法，我们可以选择性地重写需要的方法
public class WebConfig implements WebMvcConfigurer {

    /**
     * 文件上传的保存路径
     * 
     * @Value：从application.yml或application.properties配置文件中读取值
     * "${upload.path:uploads}"：
     *   - upload.path：配置文件中的属性名
     *   - :uploads：如果配置文件中没有这个属性，就使用默认值"uploads"
     * 
     * 例如：
     * 如果配置文件中写了 upload.path=D:/myfiles
     * 那么uploadPath的值就是"D:/myfiles"
     * 如果没写，就用默认的"uploads"
     */
    @Value("${upload.path:uploads}")
    private String uploadPath;

    /**
     * 配置静态资源映射 - 告诉Spring如何访问上传的文件
     * 
     * 静态资源：不需要程序处理的文件，比如图片、CSS、JS、视频等
     * 这些文件可以直接返回给浏览器，不需要经过业务逻辑处理
     * 
     * @param registry 资源处理器注册表，用来注册静态资源的映射规则
     * 
     * 工作原理：
     * 1. 前端请求：GET http://localhost:8080/uploads/avatar.jpg
     * 2. Spring看到/uploads/**，知道这是要访问静态文件
     * 3. Spring去file:D:/uploads/目录下找avatar.jpg
     * 4. 找到后直接返回给浏览器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传文件目录的绝对路径
        // 绝对路径：从磁盘根目录开始的完整路径（例如：D:/WorkSpace/uploads）
        // 相对路径：相对于当前目录的路径（例如：./uploads）
        // getAbsolutePath()会把相对路径转换成绝对路径
        String absolutePath = new File(uploadPath).getAbsolutePath();
        
        // 配置资源映射规则
        // addResourceHandler()：设置URL访问路径
        // "/uploads/**"：
        //   - /uploads/：URL的前缀
        //   - **：表示匹配所有子路径和文件
        //   - 例如：/uploads/avatar.jpg, /uploads/docs/file.pdf 都会匹配
        registry.addResourceHandler("/uploads/**")
                // addResourceLocations()：设置实际文件在服务器上的位置
                // "file:"：表示这是文件系统的路径（不是classpath路径）
                // 例如：file:D:/uploads/ 表示文件在D盘的uploads文件夹下
                .addResourceLocations("file:" + absolutePath + "/");
                
        // Windows系统的特殊处理
        // 原因：Windows使用反斜杠(\)作为路径分隔符，而URL使用正斜杠(/)
        // 需要做路径格式转换，确保在Windows系统上也能正常访问
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // file:/// 是Windows系统文件URI的标准格式
            // replace("\\", "/")：把反斜杠转换成正斜杠
            // 示例：将 Windows 本地路径转换为 URL 风格路径，如转换为 D:/uploads/
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:///" + absolutePath.replace("\\", "/") + "/");
        }
        
        // 总结：
        // 这个配置让服务器上的文件可以通过HTTP访问
        // 例如：D:/uploads/test.jpg 可以通过 http://localhost:8080/uploads/test.jpg 访问
    }
}
