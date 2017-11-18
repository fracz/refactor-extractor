/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer.demo;

import com.google.android.exoplayer.DefaultTrackSelector.TrackInfo;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.Format;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.TrackGroup;
import com.google.android.exoplayer.TrackGroupArray;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.TrackSelection;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.demo.player.DemoPlayer;

import android.media.MediaCodec.CryptoException;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Logs player events using {@link Log}.
 */
public class EventLogger implements DemoPlayer.Listener, DemoPlayer.InfoListener,
    DemoPlayer.InternalErrorListener {

  private static final String TAG = "EventLogger";
  private static final NumberFormat TIME_FORMAT;
  static {
    TIME_FORMAT = NumberFormat.getInstance(Locale.US);
    TIME_FORMAT.setMinimumFractionDigits(2);
    TIME_FORMAT.setMaximumFractionDigits(2);
  }

  private long sessionStartTimeMs;

  public void startSession() {
    sessionStartTimeMs = SystemClock.elapsedRealtime();
    Log.d(TAG, "start [0]");
  }

  public void endSession() {
    Log.d(TAG, "end [" + getSessionTimeString() + "]");
  }

  // DemoPlayer.Listener

  @Override
  public void onStateChanged(boolean playWhenReady, int state) {
    Log.d(TAG, "state [" + getSessionTimeString() + ", " + playWhenReady + ", "
        + getStateString(state) + "]");
  }

  @Override
  public void onError(Exception e) {
    Log.e(TAG, "playerFailed [" + getSessionTimeString() + "]", e);
  }

  @Override
  public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
      float pixelWidthHeightRatio) {
    Log.d(TAG, "videoSizeChanged [" + width + ", " + height + ", " + unappliedRotationDegrees
        + ", " + pixelWidthHeightRatio + "]");
  }

  @Override
  public void onTracksChanged(TrackInfo trackInfo) {
    Log.d(TAG, "Tracks [");
    // Log tracks associated to renderers.
    for (int rendererIndex = 0; rendererIndex < trackInfo.rendererCount; rendererIndex++) {
      TrackGroupArray trackGroups = trackInfo.getTrackGroups(rendererIndex);
      TrackSelection trackSelection = trackInfo.getTrackSelection(rendererIndex);
      if (trackGroups.length > 0) {
        Log.d(TAG, "  Renderer:" + rendererIndex + " [");
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
          TrackGroup trackGroup = trackGroups.get(groupIndex);
          String adaptiveSupport = getAdaptiveSupportString(
              trackGroup.length, trackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false));
          Log.d(TAG, "    Group:" + groupIndex + ", adaptive_supported=" + adaptiveSupport + " [");
          for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
            String status = getTrackStatusString(trackSelection, groupIndex, trackIndex);
            String formatSupport = getFormatSupportString(
                trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex));
            Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
                + getFormatString(trackGroup.getFormat(trackIndex))
                + ", supported=" + formatSupport);
          }
          Log.d(TAG, "    ]");
        }
        Log.d(TAG, "  ]");
      }
    }
    // Log tracks not associated with a renderer.
    TrackGroupArray trackGroups = trackInfo.getUnassociatedTrackGroups();
    if (trackGroups.length > 0) {
      Log.d(TAG, "  Renderer:None [");
      for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
        Log.d(TAG, "    Group:" + groupIndex + " [");
        TrackGroup trackGroup = trackGroups.get(groupIndex);
        for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
          String status = getTrackStatusString(false);
          String formatSupport = getFormatSupportString(TrackRenderer.FORMAT_UNSUPPORTED_TYPE);
          Log.d(TAG, "      " + status + " Track:" + trackIndex + ", "
              + getFormatString(trackGroup.getFormat(trackIndex))
              + ", supported=" + formatSupport);
        }
        Log.d(TAG, "    ]");
      }
      Log.d(TAG, "  ]");
    }
    Log.d(TAG, "]");
  }

  // DemoPlayer.InfoListener

  @Override
  public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
    Log.d(TAG, "bandwidth [" + getSessionTimeString() + ", " + bytes + ", "
        + getTimeString(elapsedMs) + ", " + bitrateEstimate + "]");
  }

  @Override
  public void onDroppedFrames(int count, long elapsed) {
    Log.d(TAG, "droppedFrames [" + getSessionTimeString() + ", " + count + "]");
  }

  @Override
  public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
      long mediaStartTimeMs, long mediaEndTimeMs) {
    // Do nothing.
  }

  @Override
  public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
       long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
    // Do nothing.
  }

  @Override
  public void onVideoFormatEnabled(Format format, int trigger, long mediaTimeMs) {
    Log.d(TAG, "videoFormat [" + getSessionTimeString() + ", " + format.id + ", "
        + Integer.toString(trigger) + "]");
  }

  @Override
  public void onAudioFormatEnabled(Format format, int trigger, long mediaTimeMs) {
    Log.d(TAG, "audioFormat [" + getSessionTimeString() + ", " + format.id + ", "
        + Integer.toString(trigger) + "]");
  }

  // DemoPlayer.InternalErrorListener

  @Override
  public void onLoadError(int sourceId, IOException e) {
    printInternalError("loadError", e);
  }

  @Override
  public void onRendererInitializationError(Exception e) {
    printInternalError("rendererInitError", e);
  }

  @Override
  public void onDrmSessionManagerError(Exception e) {
    printInternalError("drmSessionManagerError", e);
  }

  @Override
  public void onDecoderInitializationError(DecoderInitializationException e) {
    printInternalError("decoderInitializationError", e);
  }

  @Override
  public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    printInternalError("audioTrackInitializationError", e);
  }

  @Override
  public void onAudioTrackWriteError(AudioTrack.WriteException e) {
    printInternalError("audioTrackWriteError", e);
  }

  @Override
  public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
    printInternalError("audioTrackUnderrun [" + bufferSize + ", " + bufferSizeMs + ", "
        + elapsedSinceLastFeedMs + "]", null);
  }

  @Override
  public void onCryptoError(CryptoException e) {
    printInternalError("cryptoError", e);
  }

  @Override
  public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
      long initializationDurationMs) {
    Log.d(TAG, "decoderInitialized [" + getSessionTimeString() + ", " + decoderName + "]");
  }

  private void printInternalError(String type, Exception e) {
    Log.e(TAG, "internalError [" + getSessionTimeString() + ", " + type + "]", e);
  }

  private String getSessionTimeString() {
    return getTimeString(SystemClock.elapsedRealtime() - sessionStartTimeMs);
  }

  private static String getTimeString(long timeMs) {
    return TIME_FORMAT.format((timeMs) / 1000f);
  }

  private static String getStateString(int state) {
    switch (state) {
      case ExoPlayer.STATE_BUFFERING:
        return "B";
      case ExoPlayer.STATE_ENDED:
        return "E";
      case ExoPlayer.STATE_IDLE:
        return "I";
      case ExoPlayer.STATE_PREPARING:
        return "P";
      case ExoPlayer.STATE_READY:
        return "R";
      default:
        return "?";
    }
  }

  private static String getFormatSupportString(int formatSupport) {
    switch (formatSupport) {
      case TrackRenderer.FORMAT_HANDLED:
        return "YES";
      case TrackRenderer.FORMAT_EXCEEDS_CAPABILITIES:
        return "NO_EXCEEDS_CAPABILITIES";
      case TrackRenderer.FORMAT_UNSUPPORTED_SUBTYPE:
        return "NO_UNSUPPORTED_TYPE";
      case TrackRenderer.FORMAT_UNSUPPORTED_TYPE:
        return "NO";
      default:
        return "?";
    }
  }

  private static String getAdaptiveSupportString(int trackCount, int adaptiveSupport) {
    if (trackCount < 2) {
      return "N/A";
    }
    switch (adaptiveSupport) {
      case TrackRenderer.ADAPTIVE_SEAMLESS:
        return "YES";
      case TrackRenderer.ADAPTIVE_NOT_SEAMLESS:
        return "YES_NOT_SEAMLESS";
      case TrackRenderer.ADAPTIVE_NOT_SUPPORTED:
        return "NO";
      default:
        return "?";
    }
  }

  private static String getFormatString(Format format) {
    StringBuilder builder = new StringBuilder();
    builder.append("id=").append(format.id).append(", mimeType=").append(format.sampleMimeType);
    if (format.bitrate != Format.NO_VALUE) {
      builder.append(", bitrate=").append(format.bitrate);
    }
    if (format.width != -1 && format.height != -1) {
      builder.append(", res=").append(format.width).append("x").append(format.height);
    }
    if (format.frameRate != -1) {
      builder.append(", fps=").append(format.frameRate);
    }
    if (format.channelCount != -1) {
      builder.append(", channels=").append(format.channelCount);
    }
    if (format.sampleRate != -1) {
      builder.append(", sample_rate=").append(format.sampleRate);
    }
    if (format.language != null) {
      builder.append(", language=").append(format.language);
    }
    return builder.toString();
  }

  private static String getTrackStatusString(TrackSelection selection, int groupIndex,
      int trackIndex) {
    boolean groupEnabled = selection != null && selection.group == groupIndex;
    if (groupEnabled) {
      for (int i = 0; i < selection.length; i++) {
        if (selection.getTrack(i) == trackIndex) {
          return getTrackStatusString(true);
        }
      }
    }
    return getTrackStatusString(false);
  }

  private static String getTrackStatusString(boolean enabled) {
    return enabled ? "[X]" : "[ ]";
  }

}