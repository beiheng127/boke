package sub.boke.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    @GetMapping("/")
    public String root() {
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            return "redirect:" + frontendUrl + "/";
        }
        // 默认回退到后端静态资源入口（例如把前端构建后的 index.html 放到 resources/static 下）
        return "redirect:/index.html";
    }
}
