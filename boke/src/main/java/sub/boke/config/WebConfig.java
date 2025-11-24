package sub.boke.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectRoot = System.getProperty("user.dir");
        String uploadsDir = projectRoot + File.separator + "uploads" + File.separator;

        System.out.println("📁 项目根目录: " + projectRoot);
        System.out.println("📁 上传目录: " + uploadsDir);

        // Windows 系统需要特殊的文件 URL 格式
        String resourceLocation;
        if (File.separator.equals("\\")) {
            // Windows 系统：使用 file:/// 前缀
            resourceLocation = "file:///" + uploadsDir.replace("\\", "/");
        } else {
            // Linux/Mac 系统
            resourceLocation = "file:" + uploadsDir;
        }

        System.out.println("🔧 资源位置映射: " + resourceLocation);

        // 添加资源处理器
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        System.out.println("✅ 静态资源映射完成: /uploads/** -> " + resourceLocation);

        // 测试映射是否生效
        File testFile = new File(uploadsDir + "images/default/touxiang.jpg");
        System.out.println("🖼️ 测试文件存在: " + testFile.exists());
        System.out.println("🖼️ 测试文件可读: " + testFile.canRead());
    }
}