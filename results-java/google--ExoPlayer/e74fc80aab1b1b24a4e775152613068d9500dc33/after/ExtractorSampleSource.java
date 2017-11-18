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
package com.google.android.exoplayer.extractor;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.DecoderInputBuffer;
import com.google.android.exoplayer.FormatHolder;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackGroup;
import com.google.android.exoplayer.TrackGroupArray;
import com.google.android.exoplayer.TrackSelection;
import com.google.android.exoplayer.TrackStream;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;

import android.net.Uri;
import android.os.Handler;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link SampleSource} that extracts sample data using an {@link Extractor}.
 *
 * <p>If no {@link Extractor} instances are passed to the constructor, the input stream container
 * format will be detected automatically from the following supported formats:
 *
 * <ul>
 * <li>MP4, including M4A ({@link com.google.android.exoplayer.extractor.mp4.Mp4Extractor})</li>
 * <li>fMP4 ({@link com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor})</li>
 * <li>Matroska and WebM ({@link com.google.android.exoplayer.extractor.mkv.MatroskaExtractor})</li>
 * <li>Ogg Vorbis/FLAC ({@link com.google.android.exoplayer.extractor.ogg.OggExtractor}</li>
 * <li>MP3 ({@link com.google.android.exoplayer.extractor.mp3.Mp3Extractor})</li>
 * <li>AAC ({@link com.google.android.exoplayer.extractor.ts.AdtsExtractor})</li>
 * <li>MPEG TS ({@link com.google.android.exoplayer.extractor.ts.TsExtractor})</li>
 * <li>MPEG PS ({@link com.google.android.exoplayer.extractor.ts.PsExtractor})</li>
 * <li>FLV ({@link com.google.android.exoplayer.extractor.flv.FlvExtractor})</li>
 * <li>WAV ({@link com.google.android.exoplayer.extractor.wav.WavExtractor})</li>
 * <li>FLAC (only available if the FLAC extension is built and included)</li>
 * </ul>
 *
 * <p>Seeking in AAC, MPEG TS and FLV streams is not supported.
 *
 * <p>To override the default extractors, pass one or more {@link Extractor} instances to the
 * constructor. When reading a new stream, the first {@link Extractor} that returns {@code true}
 * from {@link Extractor#sniff(ExtractorInput)} will be used.
 */
public final class ExtractorSampleSource implements SampleSource, ExtractorOutput,
    Loader.Callback<Loadable> {

  /**
   * Interface definition for a callback to be notified of {@link ExtractorSampleSource} events.
   */
  public interface EventListener {

    /**
     * Invoked when an error occurs loading media data.
     *
     * @param sourceId The id of the reporting {@link SampleSource}.
     * @param e The cause of the failure.
     */
    void onLoadError(int sourceId, IOException e);

  }

  /**
   * Thrown if the input format could not recognized.
   */
  public static final class UnrecognizedInputFormatException extends ParserException {

    public UnrecognizedInputFormatException(Extractor[] extractors) {
      super("None of the available extractors ("
          + Util.getCommaDelimitedSimpleClassNames(extractors) + ") could read the stream.");
    }

  }

  /**
   * The default minimum number of times to retry loading prior to failing for on-demand streams.
   */
  public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT_ON_DEMAND = 3;

  /**
   * The default minimum number of times to retry loading prior to failing for live streams.
   */
  public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE = 6;

  private static final int MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA = -1;

  // Lazily initialized default extractor classes in priority order.
  private static List<Class<? extends Extractor>> defaultExtractorClasses;

  private final Loader<Loadable> loader;
  private final ExtractorHolder extractorHolder;
  private final Allocator allocator;
  private final int requestedBufferSize;
  private final int minLoadableRetryCount;
  private final Uri uri;
  private final DataSource dataSource;
  private final Handler eventHandler;
  private final EventListener eventListener;
  private final int eventSourceId;

  private volatile boolean tracksBuilt;
  private volatile SeekMap seekMap;

  private boolean prepared;
  private boolean seenFirstTrackSelection;
  private boolean notifyReset;
  private int enabledTrackCount;
  private DefaultTrackOutput[] sampleQueues;
  private TrackGroupArray tracks;
  private long durationUs;
  private boolean[] trackEnabledStates;

  private long downstreamPositionUs;
  private long lastSeekPositionUs;
  private long pendingResetPositionUs;

  private ExtractingLoadable loadable;
  private int extractedSamplesCountAtStartOfLoad;
  private boolean loadingFinished;

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSource A data source to read the media stream.
   * @param allocator An {@link Allocator} from which to obtain memory allocations.
   * @param requestedBufferSize The requested total buffer size for storing sample data, in bytes.
   *     The actual allocated size may exceed the value passed in if the implementation requires it.
   * @param extractors {@link Extractor}s to process the media stream. Where the possible formats
   *     are known, instantiate and inject only instances of the corresponding {@link Extractor}s.
   *     Where this is not possible, {@link #newDefaultExtractors()} can be used to construct an
   *     array of default extractors.
   */
  public ExtractorSampleSource(Uri uri, DataSource dataSource, Allocator allocator,
      int requestedBufferSize, Extractor[] extractors) {
    this(uri, dataSource, allocator, requestedBufferSize, MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA,
        extractors);
  }

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSource A data source to read the media stream.
   * @param allocator An {@link Allocator} from which to obtain memory allocations.
   * @param requestedBufferSize The requested total buffer size for storing sample data, in bytes.
   *     The actual allocated size may exceed the value passed in if the implementation requires it.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param eventSourceId An identifier that gets passed to {@code eventListener} methods.
   * @param extractors {@link Extractor}s to process the media stream. Where the possible formats
   *     are known, instantiate and inject only instances of the corresponding {@link Extractor}s.
   *     Where this is not possible, {@link #newDefaultExtractors()} can be used to construct an
   *     array of default extractors.
   */
  public ExtractorSampleSource(Uri uri, DataSource dataSource, Allocator allocator,
      int requestedBufferSize, Handler eventHandler, EventListener eventListener,
      int eventSourceId, Extractor[] extractors) {
    this(uri, dataSource, allocator, requestedBufferSize, MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA,
        eventHandler, eventListener, eventSourceId, extractors);
  }

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSource A data source to read the media stream.
   * @param allocator An {@link Allocator} from which to obtain memory allocations.
   * @param requestedBufferSize The requested total buffer size for storing sample data, in bytes.
   *     The actual allocated size may exceed the value passed in if the implementation requires it.
   * @param minLoadableRetryCount The minimum number of times that the sample source will retry
   *     if a loading error occurs.
   * @param extractors {@link Extractor}s to process the media stream. Where the possible formats
   *     are known, instantiate and inject only instances of the corresponding {@link Extractor}s.
   *     Where this is not possible, {@link #newDefaultExtractors()} can be used to construct an
   *     array of default extractors.
   */
  public ExtractorSampleSource(Uri uri, DataSource dataSource, Allocator allocator,
      int requestedBufferSize, int minLoadableRetryCount, Extractor[] extractors) {
    this(uri, dataSource, allocator, requestedBufferSize, minLoadableRetryCount, null, null, 0,
        extractors);
  }

  /**
   * @param uri The {@link Uri} of the media stream.
   * @param dataSource A data source to read the media stream.
   * @param allocator An {@link Allocator} from which to obtain memory allocations.
   * @param requestedBufferSize The requested total buffer size for storing sample data, in bytes.
   *     The actual allocated size may exceed the value passed in if the implementation requires it.
   * @param minLoadableRetryCount The minimum number of times that the sample source will retry
   *     if a loading error occurs.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param eventSourceId An identifier that gets passed to {@code eventListener} methods.
   * @param extractors {@link Extractor}s to process the media stream. Where the possible formats
   *     are known, instantiate and inject only instances of the corresponding {@link Extractor}s.
   *     Where this is not possible {@link #newDefaultExtractors()} can be used to construct an
   *     array of default extractors.
   */
  public ExtractorSampleSource(Uri uri, DataSource dataSource, Allocator allocator,
      int requestedBufferSize, int minLoadableRetryCount, Handler eventHandler,
      EventListener eventListener, int eventSourceId, Extractor[] extractors) {
    Assertions.checkState(extractors != null && extractors.length > 0);
    this.uri = uri;
    this.dataSource = dataSource;
    this.eventListener = eventListener;
    this.eventHandler = eventHandler;
    this.eventSourceId = eventSourceId;
    this.allocator = allocator;
    this.requestedBufferSize = requestedBufferSize;
    this.minLoadableRetryCount = minLoadableRetryCount;
    // Assume on-demand until we know otherwise.
    int initialMinRetryCount = minLoadableRetryCount == MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA
        ? DEFAULT_MIN_LOADABLE_RETRY_COUNT_ON_DEMAND : minLoadableRetryCount;
    loader = new Loader<>("Loader:ExtractorSampleSource", initialMinRetryCount);
    extractorHolder = new ExtractorHolder(extractors, this);
    pendingResetPositionUs = C.UNSET_TIME_US;
    sampleQueues = new DefaultTrackOutput[0];
  }

  /**
   * Builds default extractors that can be passed to an {@link ExtractorSampleSource} constructor.
   *
   * @return An array of default extractors.
   */
  public static Extractor[] newDefaultExtractors() {
    synchronized (ExtractorSampleSource.class) {
      if (defaultExtractorClasses == null) {
        // Lazily initialize defaultExtractorClasses.
        List<Class<? extends Extractor>> extractorClasses = new ArrayList<>();
        // We reference extractors using reflection so that they can be deleted cleanly.
        // Class.forName is used so that automated tools like proguard can detect the use of
        // reflection (see http://proguard.sourceforge.net/FAQ.html#forname).
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.mkv.MatroskaExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.mp4.Mp4Extractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.mp3.Mp3Extractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.ts.AdtsExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.ts.TsExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.flv.FlvExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.ogg.OggExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.ts.PsExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.extractor.wav.WavExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        try {
          extractorClasses.add(
              Class.forName("com.google.android.exoplayer.ext.flac.FlacExtractor")
              .asSubclass(Extractor.class));
        } catch (ClassNotFoundException e) {
          // Extractor not found.
        }
        defaultExtractorClasses = extractorClasses;
      }
    }

    Extractor[] extractors = new Extractor[defaultExtractorClasses.size()];
    for (int i = 0; i < extractors.length; i++) {
      try {
        extractors[i] = defaultExtractorClasses.get(i).newInstance();
      } catch (Exception e) {
        // Should never happen.
        throw new IllegalStateException("Unexpected error creating default extractor", e);
      }
    }
    return extractors;
  }

  // SampleSource implementation.

  @Override
  public boolean prepare(long positionUs) throws IOException {
    if (prepared) {
      return true;
    }
    if (seekMap != null && tracksBuilt && haveFormatsForAllTracks()) {
      int trackCount = sampleQueues.length;
      TrackGroup[] trackArray = new TrackGroup[trackCount];
      trackEnabledStates = new boolean[trackCount];
      durationUs = seekMap.getDurationUs();
      for (int i = 0; i < trackCount; i++) {
        trackArray[i] = new TrackGroup(sampleQueues[i].getUpstreamFormat());
      }
      tracks = new TrackGroupArray(trackArray);
      if (minLoadableRetryCount == MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA && !seekMap.isSeekable()
          && durationUs == C.UNSET_TIME_US) {
        loader.setMinRetryCount(DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE);
      }
      prepared = true;
      return true;
    }
    // We're not prepared.
    maybeThrowError();
    if (!loader.isLoading()) {
      startLoading();
    }
    return false;
  }

  @Override
  public long getDurationUs() {
    return durationUs;
  }

  @Override
  public TrackGroupArray getTrackGroups() {
    return tracks;
  }

  @Override
  public TrackStream[] selectTracks(List<TrackStream> oldStreams,
      List<TrackSelection> newSelections, long positionUs) {
    Assertions.checkState(prepared);
    // Unselect old tracks.
    for (int i = 0; i < oldStreams.size(); i++) {
      int track = ((TrackStreamImpl) oldStreams.get(i)).track;
      Assertions.checkState(trackEnabledStates[track]);
      enabledTrackCount--;
      trackEnabledStates[track] = false;
    }
    // Select new tracks.
    TrackStream[] newStreams = new TrackStream[newSelections.size()];
    for (int i = 0; i < newStreams.length; i++) {
      TrackSelection selection = newSelections.get(i);
      Assertions.checkState(selection.length == 1);
      Assertions.checkState(selection.getTrack(0) == 0);
      int track = selection.group;
      Assertions.checkState(!trackEnabledStates[track]);
      enabledTrackCount++;
      trackEnabledStates[track] = true;
      sampleQueues[track].needDownstreamFormat();
      newStreams[i] = new TrackStreamImpl(track);
    }
    // Cancel or start requests as necessary.
    if (enabledTrackCount == 0) {
      downstreamPositionUs = Long.MIN_VALUE;
      if (loader.isLoading()) {
        loader.cancelLoading();
      } else {
        clearState();
        allocator.trim(0);
      }
    } else if (seenFirstTrackSelection ? newStreams.length > 0 : positionUs != 0) {
      seekToInternal(positionUs);
    }
    seenFirstTrackSelection = true;
    return newStreams;
  }

  @Override
  public void continueBuffering(long playbackPositionUs) {
    downstreamPositionUs = playbackPositionUs;
    discardSamplesForDisabledTracks();
  }

  @Override
  public long readReset() {
    if (notifyReset) {
      notifyReset = false;
      return lastSeekPositionUs;
    }
    return C.UNSET_TIME_US;
  }

  @Override
  public long getBufferedPositionUs() {
    if (loadingFinished) {
      return C.END_OF_SOURCE_US;
    } else if (isPendingReset()) {
      return pendingResetPositionUs;
    } else {
      long largestQueuedTimestampUs = Long.MIN_VALUE;
      for (DefaultTrackOutput sampleQueue : sampleQueues) {
        largestQueuedTimestampUs = Math.max(largestQueuedTimestampUs,
            sampleQueue.getLargestQueuedTimestampUs());
      }
      return largestQueuedTimestampUs == Long.MIN_VALUE ? downstreamPositionUs
          : largestQueuedTimestampUs;
    }
  }

  @Override
  public void seekToUs(long positionUs) {
    seekToInternal(positionUs);
  }

  @Override
  public void release() {
    enabledTrackCount = 0;
    loader.release();
  }

  // TrackStream methods.

  /* package */ boolean isReady(int track) {
    return loadingFinished || (!isPendingReset() && !sampleQueues[track].isEmpty());
  }

  /* package */ void maybeThrowError() throws IOException {
    loader.maybeThrowError();
  }

  /* package */ int readData(int track, FormatHolder formatHolder, DecoderInputBuffer buffer) {
    if (notifyReset || isPendingReset()) {
      return TrackStream.NOTHING_READ;
    }

    return sampleQueues[track].readData(formatHolder, buffer, loadingFinished, lastSeekPositionUs);
  }

  // Loader.Callback implementation.

  @Override
  public void onLoadCompleted(Loadable loadable, long elapsedMs) {
    loadingFinished = true;
  }

  @Override
  public void onLoadCanceled(Loadable loadable, long elapsedMs) {
    if (enabledTrackCount > 0) {
      restartFrom(pendingResetPositionUs);
    } else {
      clearState();
      allocator.trim(0);
    }
  }

  @Override
  public int onLoadError(Loadable loadable, long elapsedMs, IOException e) {
    notifyLoadError(e);
    if (isLoadableExceptionFatal(e)) {
      return Loader.DONT_RETRY_FATAL;
    }
    int extractedSamplesCount = getExtractedSamplesCount();
    boolean madeProgress = extractedSamplesCount > extractedSamplesCountAtStartOfLoad;
    configureRetry(); // May clear the sample queues.
    extractedSamplesCountAtStartOfLoad = getExtractedSamplesCount();
    return madeProgress ? Loader.RETRY_RESET_ERROR_COUNT : Loader.RETRY;
  }

  // ExtractorOutput implementation.

  @Override
  public TrackOutput track(int id) {
    sampleQueues = Arrays.copyOf(sampleQueues, sampleQueues.length + 1);
    DefaultTrackOutput sampleQueue = new DefaultTrackOutput(allocator);
    sampleQueues[sampleQueues.length - 1] = sampleQueue;
    return sampleQueue;
  }

  @Override
  public void endTracks() {
    tracksBuilt = true;
  }

  @Override
  public void seekMap(SeekMap seekMap) {
    this.seekMap = seekMap;
  }

  // Internal methods.

  private void seekToInternal(long positionUs) {
    // Treat all seeks into non-seekable media as being to t=0.
    positionUs = seekMap.isSeekable() ? positionUs : 0;
    lastSeekPositionUs = positionUs;
    downstreamPositionUs = positionUs;
    notifyReset = true;
    // If we're not pending a reset, see if we can seek within the sample queues.
    boolean seekInsideBuffer = !isPendingReset();
    for (int i = 0; seekInsideBuffer && i < sampleQueues.length; i++) {
      if (trackEnabledStates[i]) {
        seekInsideBuffer = sampleQueues[i].skipToKeyframeBefore(positionUs);
      }
    }
    // If we failed to seek within the sample queues, we need to restart.
    if (!seekInsideBuffer) {
      restartFrom(positionUs);
    }
  }

  private void restartFrom(long positionUs) {
    pendingResetPositionUs = positionUs;
    loadingFinished = false;
    if (loader.isLoading()) {
      loader.cancelLoading();
    } else {
      clearState();
      startLoading();
    }
  }

  private void startLoading() {
    loadable = new ExtractingLoadable(uri, dataSource, extractorHolder, allocator,
        requestedBufferSize);
    if (prepared) {
      Assertions.checkState(isPendingReset());
      if (durationUs != C.UNSET_TIME_US && pendingResetPositionUs >= durationUs) {
        loadingFinished = true;
        pendingResetPositionUs = C.UNSET_TIME_US;
        return;
      }
      loadable.setLoadPosition(seekMap.getPosition(pendingResetPositionUs));
      pendingResetPositionUs = C.UNSET_TIME_US;
    }
    extractedSamplesCountAtStartOfLoad = getExtractedSamplesCount();
    loader.startLoading(loadable, this);
  }

  private void configureRetry() {
    Assertions.checkState(loadable != null);
    if (!prepared) {
      // We don't know whether we're playing an on-demand or a live stream. For a live stream we
      // need to load from the start, as outlined below. Since we might be playing a live stream,
      // play it safe and load from the start.
      clearSampleQueues();
      loadable.setLoadPosition(0);
    } else if (!seekMap.isSeekable() && durationUs == C.UNSET_TIME_US) {
      // We're playing a non-seekable stream with unknown duration. Assume it's live, and
      // therefore that the data at the uri is a continuously shifting window of the latest
      // available media. For this case there's no way to continue loading from where a previous
      // load finished, so it's necessary to load from the start whenever commencing a new load.
      notifyReset = true;
      clearSampleQueues();
      loadable.setLoadPosition(0);
    } else {
      // We're playing a seekable on-demand stream. Resume the current loadable, which will
      // request data starting from the point it left off.
    }
  }

  private int getExtractedSamplesCount() {
    int extractedSamplesCount = 0;
    for (DefaultTrackOutput sampleQueue : sampleQueues) {
      extractedSamplesCount += sampleQueue.getWriteIndex();
    }
    return extractedSamplesCount;
  }

  private boolean haveFormatsForAllTracks() {
    for (DefaultTrackOutput sampleQueue : sampleQueues) {
      if (sampleQueue.getUpstreamFormat() == null) {
        return false;
      }
    }
    return true;
  }

  private void discardSamplesForDisabledTracks() {
    for (int i = 0; i < trackEnabledStates.length; i++) {
      if (!trackEnabledStates[i]) {
        sampleQueues[i].skipAllSamples();
      }
    }
  }

  private void clearState() {
    clearSampleQueues();
    loadable = null;
  }

  private void clearSampleQueues() {
    for (DefaultTrackOutput sampleQueue : sampleQueues) {
      sampleQueue.clear();
    }
  }

  private boolean isPendingReset() {
    return pendingResetPositionUs != C.UNSET_TIME_US;
  }

  private boolean isLoadableExceptionFatal(IOException e) {
    return e instanceof UnrecognizedInputFormatException;
  }

  private void notifyLoadError(final IOException e) {
    if (eventHandler != null && eventListener != null) {
      eventHandler.post(new Runnable()  {
        @Override
        public void run() {
          eventListener.onLoadError(eventSourceId, e);
        }
      });
    }
  }

  private final class TrackStreamImpl implements TrackStream {

    private final int track;

    public TrackStreamImpl(int track) {
      this.track = track;
    }

    @Override
    public boolean isReady() {
      return ExtractorSampleSource.this.isReady(track);
    }

    @Override
    public void maybeThrowError() throws IOException {
      ExtractorSampleSource.this.maybeThrowError();
    }

    @Override
    public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer) {
      return ExtractorSampleSource.this.readData(track, formatHolder, buffer);
    }

  }

  /**
   * Loads the media stream and extracts sample data from it.
   */
  private static class ExtractingLoadable implements Loadable {

    private final Uri uri;
    private final DataSource dataSource;
    private final ExtractorHolder extractorHolder;
    private final Allocator allocator;
    private final int requestedBufferSize;
    private final PositionHolder positionHolder;

    private volatile boolean loadCanceled;

    private boolean pendingExtractorSeek;

    public ExtractingLoadable(Uri uri, DataSource dataSource, ExtractorHolder extractorHolder,
        Allocator allocator, int requestedBufferSize) {
      this.uri = Assertions.checkNotNull(uri);
      this.dataSource = Assertions.checkNotNull(dataSource);
      this.extractorHolder = Assertions.checkNotNull(extractorHolder);
      this.allocator = Assertions.checkNotNull(allocator);
      this.requestedBufferSize = requestedBufferSize;
      positionHolder = new PositionHolder();
      pendingExtractorSeek = true;
    }

    public void setLoadPosition(long position) {
      positionHolder.position = position;
      pendingExtractorSeek = true;
    }

    @Override
    public void cancelLoad() {
      loadCanceled = true;
    }

    @Override
    public boolean isLoadCanceled() {
      return loadCanceled;
    }

    @Override
    public void load() throws IOException, InterruptedException {
      int result = Extractor.RESULT_CONTINUE;
      while (result == Extractor.RESULT_CONTINUE && !loadCanceled) {
        ExtractorInput input = null;
        try {
          long position = positionHolder.position;
          long length = dataSource.open(new DataSpec(uri, position, C.LENGTH_UNBOUNDED, null));
          if (length != C.LENGTH_UNBOUNDED) {
            length += position;
          }
          input = new DefaultExtractorInput(dataSource, position, length);
          Extractor extractor = extractorHolder.selectExtractor(input);
          if (pendingExtractorSeek) {
            extractor.seek();
            pendingExtractorSeek = false;
          }
          while (result == Extractor.RESULT_CONTINUE && !loadCanceled) {
            allocator.blockWhileTotalBytesAllocatedExceeds(requestedBufferSize);
            result = extractor.read(input, positionHolder);
            // TODO: Implement throttling to stop us from buffering data too often.
            // TODO: Block buffering between the point at which we have sufficient data for
            // preparation to complete and the first call to endTrackSelection.
          }
        } finally {
          if (result == Extractor.RESULT_SEEK) {
            result = Extractor.RESULT_CONTINUE;
          } else if (input != null) {
            positionHolder.position = input.getPosition();
          }
          dataSource.close();
        }
      }
    }

  }

  /**
   * Stores a list of extractors and a selected extractor when the format has been detected.
   */
  private static final class ExtractorHolder {

    private final Extractor[] extractors;
    private final ExtractorOutput extractorOutput;
    private Extractor extractor;

    /**
     * Creates a holder that will select an extractor and initialize it using the specified output.
     *
     * @param extractors One or more extractors to choose from.
     * @param extractorOutput The output that will be used to initialize the selected extractor.
     */
    public ExtractorHolder(Extractor[] extractors, ExtractorOutput extractorOutput) {
      this.extractors = extractors;
      this.extractorOutput = extractorOutput;
    }

    /**
     * Returns an initialized extractor for reading {@code input}, and returns the same extractor on
     * later calls.
     *
     * @param input The {@link ExtractorInput} from which data should be read.
     * @throws UnrecognizedInputFormatException Thrown if the input format could not be detected.
     * @throws IOException Thrown if the input could not be read.
     * @throws InterruptedException Thrown if the thread was interrupted.
     */
    public Extractor selectExtractor(ExtractorInput input)
        throws IOException, InterruptedException {
      if (extractor != null) {
        return extractor;
      }
      for (Extractor extractor : extractors) {
        try {
          if (extractor.sniff(input)) {
            this.extractor = extractor;
            break;
          }
        } catch (EOFException e) {
          // Do nothing.
        } finally {
          input.resetPeekPosition();
        }
      }
      if (extractor == null) {
        throw new UnrecognizedInputFormatException(extractors);
      }
      extractor.init(extractorOutput);
      return extractor;
    }

  }

}