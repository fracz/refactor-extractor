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
package com.google.android.exoplayer.hls;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.MediaFormatHolder;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.parser.ts.TsExtractor;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.upstream.NonBlockingInputStream;
import com.google.android.exoplayer.util.Assertions;

import android.os.SystemClock;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link SampleSource} for HLS streams.
 * <p>
 * TODO: Figure out whether this should merge with the chunk package, or whether the hls
 * implementation is going to naturally diverge.
 */
public class HlsSampleSource implements SampleSource, Loader.Callback {

  private static final long MAX_SAMPLE_INTERLEAVING_OFFSET_US = 5000000;
  private static final int NO_RESET_PENDING = -1;

  private final TsExtractor.SamplePool samplePool;
  private final LoadControl loadControl;
  private final HlsChunkSource chunkSource;
  private final HlsChunkOperationHolder currentLoadableHolder;
  private final LinkedList<TsExtractor> extractors;
  private final LinkedList<TsChunk> mediaChunks;
  private final List<TsChunk> readOnlyHlsChunks;
  private final int bufferSizeContribution;
  private final boolean frameAccurateSeeking;

  private int remainingReleaseCount;
  private boolean prepared;
  private int trackCount;
  private int enabledTrackCount;
  private boolean[] trackEnabledStates;
  private boolean[] pendingDiscontinuities;
  private TrackInfo[] trackInfos;
  private MediaFormat[] downstreamMediaFormats;

  private long downstreamPositionUs;
  private long lastSeekPositionUs;
  private long pendingResetPositionUs;
  private long lastPerformedBufferOperation;

  private Loader loader;
  private IOException currentLoadableException;
  private boolean currentLoadableExceptionFatal;
  private int currentLoadableExceptionCount;
  private long currentLoadableExceptionTimestamp;

  public HlsSampleSource(HlsChunkSource chunkSource, LoadControl loadControl,
      int bufferSizeContribution, boolean frameAccurateSeeking, int downstreamRendererCount) {
    this.chunkSource = chunkSource;
    this.loadControl = loadControl;
    this.bufferSizeContribution = bufferSizeContribution;
    this.frameAccurateSeeking = frameAccurateSeeking;
    this.remainingReleaseCount = downstreamRendererCount;
    samplePool = new TsExtractor.SamplePool();
    extractors = new LinkedList<TsExtractor>();
    currentLoadableHolder = new HlsChunkOperationHolder();
    mediaChunks = new LinkedList<TsChunk>();
    readOnlyHlsChunks = Collections.unmodifiableList(mediaChunks);
  }

  @Override
  public boolean prepare() throws IOException {
    if (prepared) {
      return true;
    }
    if (loader == null) {
      loader = new Loader("Loader:HLS");
      loadControl.register(this, bufferSizeContribution);
    }
    continueBufferingInternal();
    if (extractors.isEmpty()) {
      return false;
    }
    TsExtractor extractor = extractors.get(0);
    if (extractor.isPrepared()) {
      trackCount = extractor.getTrackCount();
      trackEnabledStates = new boolean[trackCount];
      pendingDiscontinuities = new boolean[trackCount];
      downstreamMediaFormats = new MediaFormat[trackCount];
      trackInfos = new TrackInfo[trackCount];
      for (int i = 0; i < trackCount; i++) {
        MediaFormat format = extractor.getFormat(i);
        trackInfos[i] = new TrackInfo(format.mimeType, chunkSource.getDurationUs());
      }
      prepared = true;
    }
    return prepared;
  }

  @Override
  public int getTrackCount() {
    Assertions.checkState(prepared);
    return trackCount;
  }

  @Override
  public TrackInfo getTrackInfo(int track) {
    Assertions.checkState(prepared);
    return trackInfos[track];
  }

  @Override
  public void enable(int track, long positionUs) {
    Assertions.checkState(prepared);
    Assertions.checkState(!trackEnabledStates[track]);
    enabledTrackCount++;
    trackEnabledStates[track] = true;
    downstreamMediaFormats[track] = null;
    if (enabledTrackCount == 1) {
      seekToUs(positionUs);
    }
  }

  @Override
  public void disable(int track) {
    Assertions.checkState(prepared);
    Assertions.checkState(trackEnabledStates[track]);
    enabledTrackCount--;
    trackEnabledStates[track] = false;
    pendingDiscontinuities[track] = false;
    if (enabledTrackCount == 0) {
      if (loader.isLoading()) {
        loader.cancelLoading();
      } else {
        clearHlsChunks();
        clearCurrentLoadable();
      }
    }
  }

  @Override
  public boolean continueBuffering(long playbackPositionUs) throws IOException {
    Assertions.checkState(prepared);
    Assertions.checkState(enabledTrackCount > 0);
    downstreamPositionUs = playbackPositionUs;
    return continueBufferingInternal();
  }

  private boolean continueBufferingInternal() throws IOException {
    updateLoadControl();
    if (isPendingReset()) {
      return false;
    }

    TsChunk mediaChunk = mediaChunks.getFirst();
    int currentVariant = mediaChunk.variantIndex;

    TsExtractor extractor;
    if (extractors.isEmpty()) {
      extractor = new TsExtractor(mediaChunk.startTimeUs, samplePool);
      extractors.addLast(extractor);
      if (mediaChunk.discardFromFirstKeyframes) {
        extractor.discardFromNextKeyframes();
      }
    } else {
      extractor = extractors.getLast();
    }

    if (mediaChunk.isReadFinished() && mediaChunks.size() > 1) {
      discardDownstreamHlsChunk();
      mediaChunk = mediaChunks.getFirst();
      if (mediaChunk.discontinuity || mediaChunk.variantIndex != currentVariant) {
        extractor = new TsExtractor(mediaChunk.startTimeUs, samplePool);
        extractors.addLast(extractor);
      }
      if (mediaChunk.discardFromFirstKeyframes) {
        extractor.discardFromNextKeyframes();
      }
    }

    // Allow the extractor to consume from the current chunk.
    NonBlockingInputStream inputStream = mediaChunk.getNonBlockingInputStream();
    boolean haveSufficientSamples = extractor.consumeUntil(inputStream,
        downstreamPositionUs + MAX_SAMPLE_INTERLEAVING_OFFSET_US);
    if (!haveSufficientSamples) {
      // If we can't read any more, then we always say we have sufficient samples.
      haveSufficientSamples = mediaChunk.isLastChunk() && mediaChunk.isReadFinished();
    }

    if (!haveSufficientSamples && currentLoadableException != null) {
      throw currentLoadableException;
    }
    return haveSufficientSamples;
  }

  @Override
  public int readData(int track, long playbackPositionUs, MediaFormatHolder formatHolder,
      SampleHolder sampleHolder, boolean onlyReadDiscontinuity) {
    Assertions.checkState(prepared);
    downstreamPositionUs = playbackPositionUs;

    if (pendingDiscontinuities[track]) {
      pendingDiscontinuities[track] = false;
      return DISCONTINUITY_READ;
    }

    if (onlyReadDiscontinuity || isPendingReset()) {
      return NOTHING_READ;
    }

    if (extractors.isEmpty()) {
      return NOTHING_READ;
    }

    TsExtractor extractor = extractors.getFirst();
    while (extractors.size() > 1 && !extractor.hasSamples()) {
      // We're finished reading from the extractor for all tracks, and so can discard it.
      extractors.removeFirst().clear();
      extractor = extractors.getFirst();
    }
    int extractorIndex = 0;
    while (extractors.size() > extractorIndex + 1 && !extractor.hasSamples(track)) {
      // We're finished reading from the extractor for this particular track, so advance to the
      // next one for the current read.
      extractor = extractors.get(++extractorIndex);
    }

    if (!extractor.isPrepared()) {
      return NOTHING_READ;
    }

    MediaFormat mediaFormat = extractor.getFormat(track);
    if (mediaFormat != null && !mediaFormat.equals(downstreamMediaFormats[track], true)) {
      chunkSource.getMaxVideoDimensions(mediaFormat);
      formatHolder.format = mediaFormat;
      downstreamMediaFormats[track] = mediaFormat;
      return FORMAT_READ;
    }

    if (extractor.getSample(track, sampleHolder)) {
      sampleHolder.decodeOnly = frameAccurateSeeking && sampleHolder.timeUs < lastSeekPositionUs;
      return SAMPLE_READ;
    }

    TsChunk mediaChunk = mediaChunks.getFirst();
    if (mediaChunk.isLastChunk() && mediaChunk.isReadFinished()) {
      return END_OF_STREAM;
    }

    return NOTHING_READ;
  }

  @Override
  public void seekToUs(long positionUs) {
    Assertions.checkState(prepared);
    Assertions.checkState(enabledTrackCount > 0);
    downstreamPositionUs = positionUs;
    lastSeekPositionUs = positionUs;
    if (pendingResetPositionUs == positionUs) {
      return;
    }

    for (int i = 0; i < pendingDiscontinuities.length; i++) {
      pendingDiscontinuities[i] = true;
    }
    TsChunk mediaChunk = getHlsChunk(positionUs);
    if (mediaChunk == null) {
      restartFrom(positionUs);
    } else {
      discardExtractors();
      discardDownstreamHlsChunks(mediaChunk);
      mediaChunk.resetReadPosition();
      updateLoadControl();
    }
  }

  private TsChunk getHlsChunk(long positionUs) {
    Iterator<TsChunk> mediaChunkIterator = mediaChunks.iterator();
    while (mediaChunkIterator.hasNext()) {
      TsChunk mediaChunk = mediaChunkIterator.next();
      if (positionUs < mediaChunk.startTimeUs) {
        return null;
      } else if (mediaChunk.isLastChunk() || positionUs < mediaChunk.endTimeUs) {
        return mediaChunk;
      }
    }
    return null;
  }

  @Override
  public long getBufferedPositionUs() {
    Assertions.checkState(prepared);
    Assertions.checkState(enabledTrackCount > 0);
    if (isPendingReset()) {
      return pendingResetPositionUs;
    }
    TsChunk mediaChunk = mediaChunks.getLast();
    HlsChunk currentLoadable = currentLoadableHolder.chunk;
    if (currentLoadable != null && mediaChunk == currentLoadable) {
      // Linearly interpolate partially-fetched chunk times.
      long chunkLength = mediaChunk.getLength();
      if (chunkLength != C.LENGTH_UNBOUNDED) {
        return mediaChunk.startTimeUs + ((mediaChunk.endTimeUs - mediaChunk.startTimeUs)
            * mediaChunk.bytesLoaded()) / chunkLength;
      } else {
        return mediaChunk.startTimeUs;
      }
    } else if (mediaChunk.isLastChunk()) {
      return TrackRenderer.END_OF_TRACK_US;
    } else {
      return mediaChunk.endTimeUs;
    }
  }

  @Override
  public void release() {
    Assertions.checkState(remainingReleaseCount > 0);
    if (--remainingReleaseCount == 0 && loader != null) {
      loadControl.unregister(this);
      loader.release();
      loader = null;
    }
  }

  @Override
  public void onLoadCompleted(Loadable loadable) {
    HlsChunk currentLoadable = currentLoadableHolder.chunk;
    try {
      currentLoadable.consume();
    } catch (IOException e) {
      currentLoadableException = e;
      currentLoadableExceptionCount++;
      currentLoadableExceptionTimestamp = SystemClock.elapsedRealtime();
      currentLoadableExceptionFatal = true;
    } finally {
      if (!isTsChunk(currentLoadable)) {
        currentLoadable.release();
      }
      if (!currentLoadableExceptionFatal) {
        clearCurrentLoadable();
      }
      updateLoadControl();
    }
  }

  @Override
  public void onLoadCanceled(Loadable loadable) {
    HlsChunk currentLoadable = currentLoadableHolder.chunk;
    if (!isTsChunk(currentLoadable)) {
      currentLoadable.release();
    }
    clearCurrentLoadable();
    if (enabledTrackCount > 0) {
      restartFrom(pendingResetPositionUs);
    } else {
      clearHlsChunks();
      loadControl.trimAllocator();
    }
  }

  @Override
  public void onLoadError(Loadable loadable, IOException e) {
    currentLoadableException = e;
    currentLoadableExceptionCount++;
    currentLoadableExceptionTimestamp = SystemClock.elapsedRealtime();
    updateLoadControl();
  }

  private void restartFrom(long positionUs) {
    pendingResetPositionUs = positionUs;
    if (loader.isLoading()) {
      loader.cancelLoading();
    } else {
      clearHlsChunks();
      clearCurrentLoadable();
      updateLoadControl();
    }
  }

  private void clearHlsChunks() {
    discardDownstreamHlsChunks(null);
  }

  private void clearCurrentLoadable() {
    currentLoadableHolder.chunk = null;
    currentLoadableException = null;
    currentLoadableExceptionCount = 0;
    currentLoadableExceptionFatal = false;
  }

  private void updateLoadControl() {
    if (currentLoadableExceptionFatal) {
      // We've failed, but we still need to update the control with our current state.
      loadControl.update(this, downstreamPositionUs, -1, false, true);
      return;
    }

    long loadPositionUs;
    if (isPendingReset()) {
      loadPositionUs = pendingResetPositionUs;
    } else {
      TsChunk lastHlsChunk = mediaChunks.getLast();
      loadPositionUs = lastHlsChunk.nextChunkIndex == -1 ? -1 : lastHlsChunk.endTimeUs;
    }

    boolean isBackedOff = currentLoadableException != null;
    boolean nextLoader = loadControl.update(this, downstreamPositionUs, loadPositionUs,
        isBackedOff || loader.isLoading(), false);

    long now = SystemClock.elapsedRealtime();

    if (isBackedOff) {
      long elapsedMillis = now - currentLoadableExceptionTimestamp;
      if (elapsedMillis >= getRetryDelayMillis(currentLoadableExceptionCount)) {
        resumeFromBackOff();
      }
      return;
    }

    if (!loader.isLoading()) {
      if (currentLoadableHolder.chunk == null || now - lastPerformedBufferOperation > 1000) {
        lastPerformedBufferOperation = now;
        currentLoadableHolder.queueSize = readOnlyHlsChunks.size();
        chunkSource.getChunkOperation(readOnlyHlsChunks, pendingResetPositionUs,
            downstreamPositionUs, currentLoadableHolder);
        discardUpstreamHlsChunks(currentLoadableHolder.queueSize);
      }
      if (nextLoader) {
        maybeStartLoading();
      }
    }
  }

  /**
   * Resumes loading.
   * <p>
   * If the {@link HlsChunkSource} returns a chunk equivalent to the backed off chunk B, then the
   * loading of B will be resumed. In all other cases B will be discarded and the new chunk will
   * be loaded.
   */
  private void resumeFromBackOff() {
    currentLoadableException = null;

    HlsChunk backedOffChunk = currentLoadableHolder.chunk;
    if (!isTsChunk(backedOffChunk)) {
      currentLoadableHolder.queueSize = readOnlyHlsChunks.size();
      chunkSource.getChunkOperation(readOnlyHlsChunks, pendingResetPositionUs, downstreamPositionUs,
          currentLoadableHolder);
      discardUpstreamHlsChunks(currentLoadableHolder.queueSize);
      if (currentLoadableHolder.chunk == backedOffChunk) {
        // HlsChunk was unchanged. Resume loading.
        loader.startLoading(backedOffChunk, this);
      } else {
        backedOffChunk.release();
        maybeStartLoading();
      }
      return;
    }

    if (backedOffChunk == mediaChunks.getFirst()) {
      // We're not able to clear the first media chunk, so we have no choice but to continue
      // loading it.
      loader.startLoading(backedOffChunk, this);
      return;
    }

    // The current loadable is the last media chunk. Remove it before we invoke the chunk source,
    // and add it back again afterwards.
    TsChunk removedChunk = mediaChunks.removeLast();
    Assertions.checkState(backedOffChunk == removedChunk);
    currentLoadableHolder.queueSize = readOnlyHlsChunks.size();
    chunkSource.getChunkOperation(readOnlyHlsChunks, pendingResetPositionUs, downstreamPositionUs,
        currentLoadableHolder);
    mediaChunks.add(removedChunk);

    if (currentLoadableHolder.chunk == backedOffChunk) {
      // HlsChunk was unchanged. Resume loading.
      loader.startLoading(backedOffChunk, this);
    } else {
      // This call will remove and release at least one chunk from the end of mediaChunks. Since
      // the current loadable is the last media chunk, it is guaranteed to be removed.
      discardUpstreamHlsChunks(currentLoadableHolder.queueSize);
      clearCurrentLoadable();
      maybeStartLoading();
    }
  }

  private void maybeStartLoading() {
    HlsChunk currentLoadable = currentLoadableHolder.chunk;
    if (currentLoadable == null) {
      // Nothing to load.
      return;
    }
    currentLoadable.init(loadControl.getAllocator());
    if (isTsChunk(currentLoadable)) {
      TsChunk mediaChunk = (TsChunk) currentLoadable;
      mediaChunks.add(mediaChunk);
      if (isPendingReset()) {
        discardExtractors();
        pendingResetPositionUs = NO_RESET_PENDING;
      }
    }
    loader.startLoading(currentLoadable, this);
  }

  private void discardExtractors() {
    for (int i = 0; i < extractors.size(); i++) {
      extractors.get(i).clear();
    }
    extractors.clear();
  }

  /**
   * Discards downstream media chunks until {@code untilChunk} if found. {@code untilChunk} is not
   * itself discarded. Null can be passed to discard all media chunks.
   *
   * @param untilChunk The first media chunk to keep, or null to discard all media chunks.
   */
  private void discardDownstreamHlsChunks(TsChunk untilChunk) {
    if (mediaChunks.isEmpty() || untilChunk == mediaChunks.getFirst()) {
      return;
    }
    while (!mediaChunks.isEmpty() && untilChunk != mediaChunks.getFirst()) {
      mediaChunks.removeFirst().release();
    }
  }

  /**
   * Discards the first downstream media chunk.
   */
  private void discardDownstreamHlsChunk() {
    mediaChunks.removeFirst().release();
  }

  /**
   * Discard upstream media chunks until the queue length is equal to the length specified.
   *
   * @param queueLength The desired length of the queue.
   */
  private void discardUpstreamHlsChunks(int queueLength) {
    while (mediaChunks.size() > queueLength) {
      mediaChunks.removeLast().release();
    }
  }

  private boolean isTsChunk(HlsChunk chunk) {
    return chunk instanceof TsChunk;
  }

  private boolean isPendingReset() {
    return pendingResetPositionUs != NO_RESET_PENDING;
  }

  private long getRetryDelayMillis(long errorCount) {
    return Math.min((errorCount - 1) * 1000, 5000);
  }

  protected final int usToMs(long timeUs) {
    return (int) (timeUs / 1000);
  }

}