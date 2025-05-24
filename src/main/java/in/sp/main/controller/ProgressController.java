package in.sp.main.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.sp.main.Entity.VideoProgress;
import in.sp.main.Services.ProgressService;
import in.sp.main.dto.ProgressUpdateDTO;

@RestController
@RequestMapping("/api/progress")

public class ProgressController {
    private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);

    @Autowired
    private ProgressService progressService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody ProgressUpdateDTO dto) {
        try {
            logger.info("Updating progress for user {} on video {}", dto.getUserId(), dto.getVideoId());
            VideoProgress progress = progressService.updateProgress(dto);
            int percent = progressService.getProgressPercent(progress);
            Map<String, Object> response = new HashMap<>();
            response.put("percent", percent);
            response.put("resumeTime", progress.getResumeTime());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating progress", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "Failed to update video progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getProgress(
            @RequestParam String userId,
            @RequestParam String videoId) {
        try {
            logger.info("Getting progress for user {} and video {}", userId, videoId);
            VideoProgress progress = progressService.getProgress(userId, videoId);
            if (progress == null) {
                return ResponseEntity.notFound().build();
            }
            int percent = progressService.getProgressPercent(progress);
            Map<String, Object> response = new HashMap<>();
            response.put("percent", percent);
            response.put("resumeTime", progress.getResumeTime());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting progress", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", true);
            response.put("message", "Failed to get video progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
