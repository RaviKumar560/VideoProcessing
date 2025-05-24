package in.sp.main.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * Entity to track user's progress in watching a video.
 * Includes information about watched intervals, total watched time,
 * and current position in the video.
 */
@Entity
@Table(name = "video_progress",
       indexes = {
           @Index(name = "idx_user_video", columnList = "user_id,video_id", unique = true)
       })
public class VideoProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "video_id", nullable = false)
    private String videoId;

    @Min(0)
    @Column(name = "resume_time")
    private Integer resumeTime;

    @Min(0)
    @Column(name = "percent")
    private Integer percent;

    @Min(1)
    @Column(name = "video_duration")
    private Integer videoDuration;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated", nullable = false)
    private Date lastUpdated;

    @Min(0)
    @Column(name = "total_watched_time")
    private Integer totalWatchedTime;

    @Column(name = "intervals", columnDefinition = "TEXT")
    private String intervals;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getResumeTime() {
        return resumeTime;
    }

    public void setResumeTime(Integer resumeTime) {
        this.resumeTime = resumeTime;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getTotalWatchedTime() {
        return totalWatchedTime;
    }

    public void setTotalWatchedTime(Integer totalWatchedTime) {
        this.totalWatchedTime = totalWatchedTime;
    }

    public String getIntervals() {
        return intervals;
    }

    public void setIntervals(String intervals) {
        this.intervals = intervals;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }
}
