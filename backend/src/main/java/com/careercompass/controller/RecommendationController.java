package com.careercompass.controller;

import com.careercompass.dto.RecommendationDto;
import com.careercompass.service.RecommendationEngine;
import com.careercompass.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationEngine recommendations;
    private final UserService users;
    public RecommendationController(RecommendationEngine recommendations, UserService users) { this.recommendations = recommendations; this.users = users; }
    @GetMapping public List<RecommendationDto> list(Authentication auth, @RequestParam(defaultValue = "false") boolean refresh) {
        var user = users.getByEmail(auth.getName());
        return refresh || recommendations.history(user).isEmpty() ? recommendations.recommend(user) : recommendations.history(user);
    }
    @GetMapping("/{jobId}") public RecommendationDto get(Authentication auth, @PathVariable String jobId) { return recommendations.get(users.getByEmail(auth.getName()), jobId); }
}