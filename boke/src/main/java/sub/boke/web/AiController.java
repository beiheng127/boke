package sub.boke.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sub.boke.service.AiService;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    public record GenReq(String prompt, String keywords) {
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenReq req) {
        var g = aiService.generate(req.prompt(), req.keywords());
        return ResponseEntity.ok(Map.of(
                "title", g.title(),
                "summary", g.summary(),
                "content", g.content()));
    }
}

