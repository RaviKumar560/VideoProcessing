import React, { useState, useRef, useEffect, useCallback } from 'react';
import axios from 'axios';
import './App.css';

// API Configuration
const API_BASE_URL = 'http://localhost:9983';
axios.defaults.baseURL = API_BASE_URL;
axios.defaults.headers.common['Content-Type'] = 'application/json';
axios.defaults.withCredentials = true;

// API endpoints
const API_ENDPOINTS = {
  progress: '/api/progress',
  updateProgress: '/api/progress/update'
};

// Configuration
const userId = "ravi123";
const videoId = "lecture1";

// Video Configuration
const VIDEO_SOURCES = [
  'https://storage.googleapis.com/webfundamentals-assets/videos/chrome.mp4',
  'https://www.w3schools.com/html/mov_bbb.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
];
const VIDEO_URL = VIDEO_SOURCES[0];

// Utility functions
const formatTime = (seconds) => {
  if (!seconds) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

// Custom hook for video time formatting
const useTimeFormat = () => {
  return useCallback((seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }, []);
};

export default function App() {
  const videoRef = useRef(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [progress, setProgress] = useState(0);
  const [totalWatchedTime, setTotalWatchedTime] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [buffering, setBuffering] = useState(false);
  const lastUpdateRef = useRef(0);

  // Load initial progress
  useEffect(() => {
    const loadProgress = async () => {
      try {
        setLoading(true);
        const response = await axios.get(API_ENDPOINTS.progress, {
          params: { userId, videoId }
        });
        
        if (response.data) {
          setProgress(response.data.percent || 0);
          setTotalWatchedTime(response.data.totalWatchedTime || 0);
          setLastUpdated(response.data.lastUpdated);
        }
      } catch (error) {
        console.error('Error loading progress:', error);
        let errorMessage = 'An error occurred while loading progress.';
        
        if (error.response) {
          errorMessage = `Server error: ${error.response.status}`;
          if (error.response.data && error.response.data.message) {
            errorMessage += ` - ${error.response.data.message}`;
          }
        } else if (error.request) {
          errorMessage = 'Could not connect to the server. Please check if the backend is running.';
        }
        
        setError(errorMessage);
      } finally {
        setLoading(false);
      }
    };

    loadProgress();
  }, [userId, videoId]);

  const handleTimeUpdate = useCallback(async () => {
    if (!videoRef.current) return;

    const now = Date.now();
    if (now - lastUpdateRef.current < 1000) return; // Throttle updates to once per second
    lastUpdateRef.current = now;

    const currentTime = Math.floor(videoRef.current.currentTime);
    const duration = Math.floor(videoRef.current.duration);

    setCurrentTime(currentTime);
    setDuration(duration);

    try {
      const response = await axios.post(API_ENDPOINTS.updateProgress, {
        userId,
        videoId,
        currentTime,
        duration,
        timestamp: new Date().toISOString()
      });

      if (response.data) {
        setProgress(response.data.percent || 0);
        setTotalWatchedTime(response.data.totalWatchedTime || 0);
        setLastUpdated(response.data.lastUpdated);
      }
    } catch (error) {
      console.error('Error updating progress:', error);
    }
  }, []);

  const handleWaiting = useCallback(() => {
    setBuffering(true);
  }, []);

  const handlePlaying = useCallback(() => {
    setBuffering(false);
  }, []);

  return (
    <div className="video-container">
      <h1>Video Progress Analysis</h1>
      {error && <div className="error-message">{error}</div>}
      
      <div className="video-wrapper">
        {loading && <div className="loading-overlay">Loading video...</div>}
        {buffering && <div className="buffering-overlay">Buffering...</div>}
        <video
          ref={videoRef}
          controls
          onLoadStart={() => setLoading(true)}
          onLoadedData={() => setLoading(false)}
          onTimeUpdate={handleTimeUpdate}
          onWaiting={handleWaiting}
          onPlaying={handlePlaying}
          onError={() => {
            setError('Error loading video. Please try again.');
            setLoading(false);
          }}
        >
          <source src="/sample.mp4" type="video/mp4" />

          Your browser does not support the video tag.
        </video>
      </div>

      <div className="progress-info">
        <div className="progress-stats">
          <div className="stat-item">
            <h3>Watch Progress</h3>
            <div className="progress-bar">
              <div 
                className="progress-bar-fill" 
                style={{ width: `${progress}%` }}
              ></div>
            </div>
            <p>{progress}% completed</p>
          </div>

          <div className="stat-item">
            <h3>Time Statistics</h3>
            <p>Current Position: {formatTime(currentTime)}</p>
            <p>Video Duration: {formatTime(duration)}</p>
            <p>Total Watched: {formatTime(totalWatchedTime)}</p>
          </div>

          <div className="stat-item">
            <h3>Session Info</h3>
            <p>User ID: {userId}</p>
            <p>Video ID: {videoId}</p>
            <p>Last Updated: {formatDate(lastUpdated)}</p>
          </div>
        </div>
        <p className="help-text">
          Your progress is automatically saved as you watch. 
          Skipped sections are not counted towards progress.
        </p>
      </div>
    </div>
  );
}
