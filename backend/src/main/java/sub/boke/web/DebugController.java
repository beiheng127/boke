package sub.boke.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;

@RestController
@RequestMapping("/test")
public class DebugController {

    @GetMapping("/debug/static-resources")
    public String debugStaticResources() {
        StringBuilder result = new StringBuilder();

        String projectRoot = System.getProperty("user.dir");
        String uploadsPath = projectRoot + "/uploads/";
        String defaultAvatarPath = uploadsPath + "images/default/touxiang.jpg";

        result.append("=== 静态资源调试信息 ===<br>");
        result.append("项目根目录: ").append(projectRoot).append("<br>");
        result.append("上传目录: ").append(uploadsPath).append("<br>");

        File uploadDir = new File(uploadsPath);
        result.append("上传目录存在: ").append(uploadDir.exists()).append("<br>");
        result.append("上传目录是目录: ").append(uploadDir.isDirectory()).append("<br>");
        result.append("上传目录可读: ").append(uploadDir.canRead()).append("<br>");

        File defaultAvatar = new File(defaultAvatarPath);
        result.append("默认头像完整路径: ").append(defaultAvatarPath).append("<br>");
        result.append("默认头像存在: ").append(defaultAvatar.exists()).append("<br>");
        result.append("默认头像可读: ").append(defaultAvatar.canRead()).append("<br>");
        result.append("默认头像大小: ").append(defaultAvatar.exists() ? defaultAvatar.length() : 0).append(" bytes<br>");

        // 列出 uploads 目录结构
        result.append("<br>=== 上传目录结构 ===<br>");
        listDirectory(uploadDir, result, 0);

        return result.toString().replace("\n", "<br>");
    }

    private void listDirectory(File dir, StringBuilder result, int depth) {
        if (!dir.exists() || !dir.isDirectory()) {
            result.append("目录不存在或不是目录<br>");
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            result.append("无法读取目录内容<br>");
            return;
        }

        for (File file : files) {
            for (int i = 0; i < depth; i++) {
                result.append("&nbsp;&nbsp;");
            }
            result.append(file.getName());
            if (file.isDirectory()) {
                result.append("/<br>");
                listDirectory(file, result, depth + 1);
            } else {
                result.append(" (").append(file.length()).append(" bytes)<br>");
            }
        }
    }



    //测试接口
    @GetMapping("/test-avatar")
    public ResponseEntity<String> testAvatar() {
        String uploadsDir = System.getProperty("user.dir") + "/uploads/";
        File file = new File(uploadsDir, "images/default/touxiang.jpg");
        if (file.exists()) {
            return ResponseEntity.ok("文件存在: " + file.getAbsolutePath());
        } else {
            return ResponseEntity.status(404).body("文件不存在: " + file.getAbsolutePath());
        }
    }
}