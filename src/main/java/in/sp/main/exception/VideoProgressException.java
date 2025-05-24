package in.sp.main.exception;

/**
 * Custom exception for video progress related errors.
 */
public class VideoProgressException extends RuntimeException {
    
    public VideoProgressException(String message) {
        super(message);
    }

    public VideoProgressException(String message, Throwable cause) {
        super(message, cause);
    }
}
