package in.sp.main.Services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.sp.main.Entity.VideoProgress;
import in.sp.main.Repository.VideoProgressRepository;
import in.sp.main.dto.ProgressUpdateDTO;
import in.sp.main.exception.VideoProgressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for managing video progress tracking.
 * Handles operations like updating progress, calculating watched time,
 * and managing watched intervals.
 */
@Service
public class ProgressService {
    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    @Autowired
    private VideoProgressRepository progressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Updates the video progress for a user.
     * Creates a new progress entry if none exists, or updates the existing one.
     *
     * @param dto The progress update data transfer object
     * @return Updated VideoProgress entity
     * @throws VideoProgressException if there's an error processing the update
     */
    @Transactional
    public VideoProgress updateProgress(ProgressUpdateDTO dto) {
        logger.info("Updating progress for user {} on video {}", dto.getUserId(), dto.getVideoId());

        VideoProgress progress = progressRepository.findByUserIdAndVideoId(dto.getUserId(), dto.getVideoId())
                .orElseGet(() -> {
                    VideoProgress p = new VideoProgress();
                    p.setUserId(dto.getUserId());
                    p.setVideoId(dto.getVideoId());
                    p.setResumeTime(dto.getCurrentTime());
                    p.setLastUpdated(new Date());
                    p.setPercent(0);
                    p.setTotalWatchedTime(0);
                    p.setIntervals("[]");
                    return p;
                });

        try {
            List<int[]> intervals;
            String watchedIntervalsJson = progress.getIntervals();
            if (watchedIntervalsJson == null || watchedIntervalsJson.isEmpty()) {
                intervals = new ArrayList<>();
            } else {
                try {
                    TypeReference<List<int[]>> typeRef = new TypeReference<List<int[]>>() {};
                    intervals = objectMapper.readValue(watchedIntervalsJson, typeRef);
                } catch (JsonProcessingException e) {
                    throw new VideoProgressException("Error parsing intervals JSON", e);
                }
            }
            
            // Add new interval
            intervals.add(new int[]{dto.getCurrentTime(), dto.getCurrentTime() + 1});
            
            // Merge overlapping intervals
            intervals = mergeIntervals(intervals);
            
            // Update progress
            progress.setIntervals(objectMapper.writeValueAsString(intervals));
            progress.setResumeTime(dto.getCurrentTime());
            progress.setLastUpdated(new Date());

            // Calculate progress percentage
            int percent = calculateProgressPercent(intervals, dto.getDuration());
            progress.setPercent(percent);

            // Calculate total watched time
            int totalWatchedTime = 0;
            for (int[] interval : intervals) {
                totalWatchedTime += interval[1] - interval[0];
            }
            progress.setTotalWatchedTime(totalWatchedTime);
            progress.setVideoDuration(dto.getDuration());
            
            try {
                return progressRepository.save(progress);
            } catch (Exception e) {
                throw new VideoProgressException("Error saving video progress", e);
            }
        } catch (Exception e) {
            logger.error("Error updating progress", e);
            throw new RuntimeException("Failed to update video progress", e);
        }
    }

    private int calculateProgressPercent(List<int[]> intervals, int duration) {
        if (duration <= 0) return 0;
        
        int totalWatched = 0;
        for (int[] interval : intervals) {
            totalWatched += interval[1] - interval[0];
        }
        
        return Math.min(100, (int) (((double) totalWatched / duration) * 100));
    }

    private List<int[]> mergeIntervals(List<int[]> intervals) {
        if (intervals == null || intervals.isEmpty()) return new ArrayList<>();
        
        // Sort intervals by start time
        intervals.sort(Comparator.comparingInt(a -> a[0]));
        
        LinkedList<int[]> merged = new LinkedList<>();
        for (int[] interval : intervals) {
            // If list is empty or no overlap with last interval
            if (merged.isEmpty() || merged.getLast()[1] < interval[0]) {
                merged.add(interval);
            }
            // If there is overlap, merge the intervals
            else {
                merged.getLast()[1] = Math.max(merged.getLast()[1], interval[1]);
            }
        }
        return merged;
    }

    public VideoProgress getProgress(String userId, String videoId) {
        logger.info("Fetching progress for user {} on video {}", userId, videoId);
        
        try {
            return progressRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseGet(() -> {
                    VideoProgress progress = new VideoProgress();
                    progress.setUserId(userId);
                    progress.setVideoId(videoId);
                    progress.setIntervals("[]");
                    progress.setPercent(0);
                    progress.setLastUpdated(Date.from(Instant.now()));
                    return progress;
                });
        } catch (Exception e) {
            logger.error("Error fetching progress", e);
            throw new RuntimeException("Failed to fetch video progress", e);
        }
    }

    public int getProgressPercent(VideoProgress progress) {
        if (progress == null || progress.getVideoDuration() <= 0) return 0;
        try {
            List<int[]> intervals;
            String watchedIntervalsJson = progress.getIntervals();
            if (watchedIntervalsJson == null || watchedIntervalsJson.isEmpty()) {
                return 0;
            }
            TypeReference<List<int[]>> typeRef = new TypeReference<List<int[]>>() {};
            intervals = objectMapper.readValue(watchedIntervalsJson, typeRef);
            return calculateProgressPercent(intervals, progress.getVideoDuration());
        } catch (Exception e) {
            logger.error("Error calculating progress percent", e);
            return 0;
        }
    }
}
