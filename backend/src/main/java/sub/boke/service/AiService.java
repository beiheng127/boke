package sub.boke.service;

import org.springframework.stereotype.Service;

@Service
public class AiService {
    public record Generated(String title, String summary, String content) {
    }

    public Generated generate(String prompt, String keywords) {
        String title = (prompt == null || prompt.isBlank()) ? "我的新文章" : prompt.trim();
        String summary = "这是一篇基于关键词【" + (keywords == null ? "" : keywords) + "】生成的文章摘要，供快速发布。";
        StringBuilder content = new StringBuilder();
        content.append("# ").append(title).append("\n\n");
        content.append("引言：围绕主题“").append(title).append("”展开，结合关键词“").append(keywords == null ? "" : keywords)
                .append("”。\n\n");
        for (int i = 1; i <= 5; i++) {
            content.append("## 小节 ").append(i).append("\n");
            content.append("在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n");
        }
        content.append("结语：总结全文观点，并提出可落地的行动建议。\n");
        return new Generated(title, summary, content.toString());
    }
}

