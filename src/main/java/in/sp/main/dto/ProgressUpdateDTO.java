package in.sp.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * Data Transfer Object for video progress updates.
 * Used to track user's progress in watching a video.
 */
public class ProgressUpdateDTO {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Video ID is required")
    private String videoId;

    @NotNull(message = "Current time is required")
    @Min(value = 0, message = "Current time must be non-negative")
    private Integer currentTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    private Integer duration;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Integer currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
