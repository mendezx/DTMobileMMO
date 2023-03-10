package com.google.android.exoplayer2.source.chunk;

import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.SampleQueue;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.source.SequenceableLoader;
import com.google.android.exoplayer2.source.chunk.ChunkSource;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.Loader;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class ChunkSampleStream<T extends ChunkSource> implements SampleStream, SequenceableLoader, Loader.Callback<Chunk>, Loader.ReleaseCallback {
    private static final String TAG = "ChunkSampleStream";
    private final SequenceableLoader.Callback<ChunkSampleStream<T>> callback;
    private final T chunkSource;
    long decodeOnlyUntilPositionUs;
    private final SampleQueue[] embeddedSampleQueues;
    private final Format[] embeddedTrackFormats;
    private final int[] embeddedTrackTypes;
    private final boolean[] embeddedTracksSelected;
    private final MediaSourceEventListener.EventDispatcher eventDispatcher;
    private long lastSeekPositionUs;
    boolean loadingFinished;
    private final BaseMediaChunkOutput mediaChunkOutput;
    private final int minLoadableRetryCount;
    private long pendingResetPositionUs;
    private Format primaryDownstreamTrackFormat;
    private final SampleQueue primarySampleQueue;
    public final int primaryTrackType;
    @Nullable
    private ReleaseCallback<T> releaseCallback;
    private final Loader loader = new Loader("Loader:ChunkSampleStream");
    private final ChunkHolder nextChunkHolder = new ChunkHolder();
    private final ArrayList<BaseMediaChunk> mediaChunks = new ArrayList<>();
    private final List<BaseMediaChunk> readOnlyMediaChunks = Collections.unmodifiableList(this.mediaChunks);

    /* loaded from: classes.dex */
    public interface ReleaseCallback<T extends ChunkSource> {
        void onSampleStreamReleased(ChunkSampleStream<T> chunkSampleStream);
    }

    public ChunkSampleStream(int primaryTrackType, int[] embeddedTrackTypes, Format[] embeddedTrackFormats, T chunkSource, SequenceableLoader.Callback<ChunkSampleStream<T>> callback, Allocator allocator, long positionUs, int minLoadableRetryCount, MediaSourceEventListener.EventDispatcher eventDispatcher) {
        this.primaryTrackType = primaryTrackType;
        this.embeddedTrackTypes = embeddedTrackTypes;
        this.embeddedTrackFormats = embeddedTrackFormats;
        this.chunkSource = chunkSource;
        this.callback = callback;
        this.eventDispatcher = eventDispatcher;
        this.minLoadableRetryCount = minLoadableRetryCount;
        int embeddedTrackCount = embeddedTrackTypes == null ? 0 : embeddedTrackTypes.length;
        this.embeddedSampleQueues = new SampleQueue[embeddedTrackCount];
        this.embeddedTracksSelected = new boolean[embeddedTrackCount];
        int[] trackTypes = new int[embeddedTrackCount + 1];
        SampleQueue[] sampleQueues = new SampleQueue[embeddedTrackCount + 1];
        this.primarySampleQueue = new SampleQueue(allocator);
        trackTypes[0] = primaryTrackType;
        sampleQueues[0] = this.primarySampleQueue;
        for (int i = 0; i < embeddedTrackCount; i++) {
            SampleQueue sampleQueue = new SampleQueue(allocator);
            this.embeddedSampleQueues[i] = sampleQueue;
            sampleQueues[i + 1] = sampleQueue;
            trackTypes[i + 1] = embeddedTrackTypes[i];
        }
        this.mediaChunkOutput = new BaseMediaChunkOutput(trackTypes, sampleQueues);
        this.pendingResetPositionUs = positionUs;
        this.lastSeekPositionUs = positionUs;
    }

    public void discardBuffer(long positionUs, boolean toKeyframe) {
        int oldFirstIndex = this.primarySampleQueue.getFirstIndex();
        this.primarySampleQueue.discardTo(positionUs, toKeyframe, true);
        int newFirstIndex = this.primarySampleQueue.getFirstIndex();
        if (newFirstIndex > oldFirstIndex) {
            long discardToUs = this.primarySampleQueue.getFirstTimestampUs();
            for (int i = 0; i < this.embeddedSampleQueues.length; i++) {
                this.embeddedSampleQueues[i].discardTo(discardToUs, toKeyframe, this.embeddedTracksSelected[i]);
            }
            discardDownstreamMediaChunks(newFirstIndex);
        }
    }

    public ChunkSampleStream<T>.EmbeddedSampleStream selectEmbeddedTrack(long positionUs, int trackType) {
        for (int i = 0; i < this.embeddedSampleQueues.length; i++) {
            if (this.embeddedTrackTypes[i] == trackType) {
                Assertions.checkState(!this.embeddedTracksSelected[i]);
                this.embeddedTracksSelected[i] = true;
                this.embeddedSampleQueues[i].rewind();
                this.embeddedSampleQueues[i].advanceTo(positionUs, true, true);
                return new EmbeddedSampleStream(this, this.embeddedSampleQueues[i], i);
            }
        }
        throw new IllegalStateException();
    }

    public T getChunkSource() {
        return this.chunkSource;
    }

    @Override // com.google.android.exoplayer2.source.SequenceableLoader
    public long getBufferedPositionUs() {
        BaseMediaChunk lastCompletedMediaChunk;
        if (this.loadingFinished) {
            return Long.MIN_VALUE;
        }
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        long bufferedPositionUs = this.lastSeekPositionUs;
        BaseMediaChunk lastMediaChunk = getLastMediaChunk();
        if (lastMediaChunk.isLoadCompleted()) {
            lastCompletedMediaChunk = lastMediaChunk;
        } else {
            lastCompletedMediaChunk = this.mediaChunks.size() > 1 ? this.mediaChunks.get(this.mediaChunks.size() - 2) : null;
        }
        if (lastCompletedMediaChunk != null) {
            bufferedPositionUs = Math.max(bufferedPositionUs, lastCompletedMediaChunk.endTimeUs);
        }
        return Math.max(bufferedPositionUs, this.primarySampleQueue.getLargestQueuedTimestampUs());
    }

    public long getAdjustedSeekPositionUs(long positionUs, SeekParameters seekParameters) {
        return this.chunkSource.getAdjustedSeekPositionUs(positionUs, seekParameters);
    }

    public void seekToUs(long positionUs) {
        boolean seekInsideBuffer;
        SampleQueue[] sampleQueueArr;
        this.lastSeekPositionUs = positionUs;
        this.primarySampleQueue.rewind();
        if (isPendingReset()) {
            seekInsideBuffer = false;
        } else {
            BaseMediaChunk seekToMediaChunk = null;
            int i = 0;
            while (true) {
                if (i >= this.mediaChunks.size()) {
                    break;
                }
                BaseMediaChunk mediaChunk = this.mediaChunks.get(i);
                long mediaChunkStartTimeUs = mediaChunk.startTimeUs;
                if (mediaChunkStartTimeUs == positionUs && mediaChunk.seekTimeUs == C.TIME_UNSET) {
                    seekToMediaChunk = mediaChunk;
                    break;
                } else if (mediaChunkStartTimeUs > positionUs) {
                    break;
                } else {
                    i++;
                }
            }
            if (seekToMediaChunk != null) {
                seekInsideBuffer = this.primarySampleQueue.setReadPosition(seekToMediaChunk.getFirstSampleIndex(0));
                this.decodeOnlyUntilPositionUs = Long.MIN_VALUE;
            } else {
                seekInsideBuffer = this.primarySampleQueue.advanceTo(positionUs, true, (positionUs > getNextLoadPositionUs() ? 1 : (positionUs == getNextLoadPositionUs() ? 0 : -1)) < 0) != -1;
                this.decodeOnlyUntilPositionUs = this.lastSeekPositionUs;
            }
        }
        if (seekInsideBuffer) {
            for (SampleQueue embeddedSampleQueue : this.embeddedSampleQueues) {
                embeddedSampleQueue.rewind();
                embeddedSampleQueue.advanceTo(positionUs, true, false);
            }
            return;
        }
        this.pendingResetPositionUs = positionUs;
        this.loadingFinished = false;
        this.mediaChunks.clear();
        if (this.loader.isLoading()) {
            this.loader.cancelLoading();
            return;
        }
        this.primarySampleQueue.reset();
        for (SampleQueue embeddedSampleQueue2 : this.embeddedSampleQueues) {
            embeddedSampleQueue2.reset();
        }
    }

    public void release() {
        release(null);
    }

    public void release(@Nullable ReleaseCallback<T> callback) {
        SampleQueue[] sampleQueueArr;
        this.releaseCallback = callback;
        this.primarySampleQueue.discardToEnd();
        for (SampleQueue embeddedSampleQueue : this.embeddedSampleQueues) {
            embeddedSampleQueue.discardToEnd();
        }
        this.loader.release(this);
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.ReleaseCallback
    public void onLoaderReleased() {
        SampleQueue[] sampleQueueArr;
        this.primarySampleQueue.reset();
        for (SampleQueue embeddedSampleQueue : this.embeddedSampleQueues) {
            embeddedSampleQueue.reset();
        }
        if (this.releaseCallback != null) {
            this.releaseCallback.onSampleStreamReleased(this);
        }
    }

    @Override // com.google.android.exoplayer2.source.SampleStream
    public boolean isReady() {
        return this.loadingFinished || (!isPendingReset() && this.primarySampleQueue.hasNextSample());
    }

    @Override // com.google.android.exoplayer2.source.SampleStream
    public void maybeThrowError() throws IOException {
        this.loader.maybeThrowError();
        if (!this.loader.isLoading()) {
            this.chunkSource.maybeThrowError();
        }
    }

    @Override // com.google.android.exoplayer2.source.SampleStream
    public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean formatRequired) {
        if (isPendingReset()) {
            return -3;
        }
        int result = this.primarySampleQueue.read(formatHolder, buffer, formatRequired, this.loadingFinished, this.decodeOnlyUntilPositionUs);
        if (result == -4) {
            maybeNotifyPrimaryTrackFormatChanged(this.primarySampleQueue.getReadIndex(), 1);
            return result;
        }
        return result;
    }

    @Override // com.google.android.exoplayer2.source.SampleStream
    public int skipData(long positionUs) {
        int skipCount;
        if (isPendingReset()) {
            return 0;
        }
        if (this.loadingFinished && positionUs > this.primarySampleQueue.getLargestQueuedTimestampUs()) {
            skipCount = this.primarySampleQueue.advanceToEnd();
        } else {
            skipCount = this.primarySampleQueue.advanceTo(positionUs, true, true);
            if (skipCount == -1) {
                skipCount = 0;
            }
        }
        if (skipCount > 0) {
            maybeNotifyPrimaryTrackFormatChanged(this.primarySampleQueue.getReadIndex(), skipCount);
            return skipCount;
        }
        return skipCount;
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.Callback
    public void onLoadCompleted(Chunk loadable, long elapsedRealtimeMs, long loadDurationMs) {
        this.chunkSource.onChunkLoadCompleted(loadable);
        this.eventDispatcher.loadCompleted(loadable.dataSpec, loadable.type, this.primaryTrackType, loadable.trackFormat, loadable.trackSelectionReason, loadable.trackSelectionData, loadable.startTimeUs, loadable.endTimeUs, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded());
        this.callback.onContinueLoadingRequested(this);
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.Callback
    public void onLoadCanceled(Chunk loadable, long elapsedRealtimeMs, long loadDurationMs, boolean released) {
        SampleQueue[] sampleQueueArr;
        this.eventDispatcher.loadCanceled(loadable.dataSpec, loadable.type, this.primaryTrackType, loadable.trackFormat, loadable.trackSelectionReason, loadable.trackSelectionData, loadable.startTimeUs, loadable.endTimeUs, elapsedRealtimeMs, loadDurationMs, loadable.bytesLoaded());
        if (!released) {
            this.primarySampleQueue.reset();
            for (SampleQueue embeddedSampleQueue : this.embeddedSampleQueues) {
                embeddedSampleQueue.reset();
            }
            this.callback.onContinueLoadingRequested(this);
        }
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.Callback
    public int onLoadError(Chunk loadable, long elapsedRealtimeMs, long loadDurationMs, IOException error) {
        long bytesLoaded = loadable.bytesLoaded();
        boolean isMediaChunk = isMediaChunk(loadable);
        int lastChunkIndex = this.mediaChunks.size() - 1;
        boolean cancelable = (bytesLoaded != 0 && isMediaChunk && haveReadFromMediaChunk(lastChunkIndex)) ? false : true;
        boolean canceled = false;
        if (this.chunkSource.onChunkLoadError(loadable, cancelable, error)) {
            if (!cancelable) {
                Log.w(TAG, "Ignoring attempt to cancel non-cancelable load.");
            } else {
                canceled = true;
                if (isMediaChunk) {
                    BaseMediaChunk removed = discardUpstreamMediaChunksFromIndex(lastChunkIndex);
                    Assertions.checkState(removed == loadable);
                    if (this.mediaChunks.isEmpty()) {
                        this.pendingResetPositionUs = this.lastSeekPositionUs;
                    }
                }
            }
        }
        this.eventDispatcher.loadError(loadable.dataSpec, loadable.type, this.primaryTrackType, loadable.trackFormat, loadable.trackSelectionReason, loadable.trackSelectionData, loadable.startTimeUs, loadable.endTimeUs, elapsedRealtimeMs, loadDurationMs, bytesLoaded, error, canceled);
        if (canceled) {
            this.callback.onContinueLoadingRequested(this);
            return 2;
        }
        return 0;
    }

    @Override // com.google.android.exoplayer2.source.SequenceableLoader
    public boolean continueLoading(long positionUs) {
        MediaChunk previousChunk;
        long loadPositionUs;
        if (this.loadingFinished || this.loader.isLoading()) {
            return false;
        }
        boolean pendingReset = isPendingReset();
        if (pendingReset) {
            previousChunk = null;
            loadPositionUs = this.pendingResetPositionUs;
        } else {
            previousChunk = getLastMediaChunk();
            loadPositionUs = previousChunk.endTimeUs;
        }
        this.chunkSource.getNextChunk(previousChunk, positionUs, loadPositionUs, this.nextChunkHolder);
        boolean endOfStream = this.nextChunkHolder.endOfStream;
        Chunk loadable = this.nextChunkHolder.chunk;
        this.nextChunkHolder.clear();
        if (endOfStream) {
            this.pendingResetPositionUs = C.TIME_UNSET;
            this.loadingFinished = true;
            return true;
        } else if (loadable == null) {
            return false;
        } else {
            if (isMediaChunk(loadable)) {
                BaseMediaChunk mediaChunk = (BaseMediaChunk) loadable;
                if (pendingReset) {
                    boolean resetToMediaChunk = mediaChunk.startTimeUs == this.pendingResetPositionUs;
                    this.decodeOnlyUntilPositionUs = resetToMediaChunk ? Long.MIN_VALUE : this.pendingResetPositionUs;
                    this.pendingResetPositionUs = C.TIME_UNSET;
                }
                mediaChunk.init(this.mediaChunkOutput);
                this.mediaChunks.add(mediaChunk);
            }
            long elapsedRealtimeMs = this.loader.startLoading(loadable, this, this.minLoadableRetryCount);
            this.eventDispatcher.loadStarted(loadable.dataSpec, loadable.type, this.primaryTrackType, loadable.trackFormat, loadable.trackSelectionReason, loadable.trackSelectionData, loadable.startTimeUs, loadable.endTimeUs, elapsedRealtimeMs);
            return true;
        }
    }

    @Override // com.google.android.exoplayer2.source.SequenceableLoader
    public long getNextLoadPositionUs() {
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        if (this.loadingFinished) {
            return Long.MIN_VALUE;
        }
        return getLastMediaChunk().endTimeUs;
    }

    @Override // com.google.android.exoplayer2.source.SequenceableLoader
    public void reevaluateBuffer(long positionUs) {
        int currentQueueSize;
        int preferredQueueSize;
        if (!this.loader.isLoading() && !isPendingReset() && (currentQueueSize = this.mediaChunks.size()) > (preferredQueueSize = this.chunkSource.getPreferredQueueSize(positionUs, this.readOnlyMediaChunks))) {
            int newQueueSize = currentQueueSize;
            int i = preferredQueueSize;
            while (true) {
                if (i >= currentQueueSize) {
                    break;
                } else if (haveReadFromMediaChunk(i)) {
                    i++;
                } else {
                    newQueueSize = i;
                    break;
                }
            }
            if (newQueueSize != currentQueueSize) {
                long endTimeUs = getLastMediaChunk().endTimeUs;
                BaseMediaChunk firstRemovedChunk = discardUpstreamMediaChunksFromIndex(newQueueSize);
                if (this.mediaChunks.isEmpty()) {
                    this.pendingResetPositionUs = this.lastSeekPositionUs;
                }
                this.loadingFinished = false;
                this.eventDispatcher.upstreamDiscarded(this.primaryTrackType, firstRemovedChunk.startTimeUs, endTimeUs);
            }
        }
    }

    private boolean isMediaChunk(Chunk chunk) {
        return chunk instanceof BaseMediaChunk;
    }

    private boolean haveReadFromMediaChunk(int mediaChunkIndex) {
        BaseMediaChunk mediaChunk = this.mediaChunks.get(mediaChunkIndex);
        if (this.primarySampleQueue.getReadIndex() > mediaChunk.getFirstSampleIndex(0)) {
            return true;
        }
        for (int i = 0; i < this.embeddedSampleQueues.length; i++) {
            if (this.embeddedSampleQueues[i].getReadIndex() > mediaChunk.getFirstSampleIndex(i + 1)) {
                return true;
            }
        }
        return false;
    }

    boolean isPendingReset() {
        return this.pendingResetPositionUs != C.TIME_UNSET;
    }

    private void discardDownstreamMediaChunks(int discardToPrimaryStreamIndex) {
        int discardToMediaChunkIndex = primaryStreamIndexToMediaChunkIndex(discardToPrimaryStreamIndex, 0);
        if (discardToMediaChunkIndex > 0) {
            Util.removeRange(this.mediaChunks, 0, discardToMediaChunkIndex);
        }
    }

    private void maybeNotifyPrimaryTrackFormatChanged(int toPrimaryStreamReadIndex, int readCount) {
        int fromMediaChunkIndex = primaryStreamIndexToMediaChunkIndex(toPrimaryStreamReadIndex - readCount, 0);
        int toMediaChunkIndexInclusive = readCount == 1 ? fromMediaChunkIndex : primaryStreamIndexToMediaChunkIndex(toPrimaryStreamReadIndex - 1, fromMediaChunkIndex);
        for (int i = fromMediaChunkIndex; i <= toMediaChunkIndexInclusive; i++) {
            maybeNotifyPrimaryTrackFormatChanged(i);
        }
    }

    private void maybeNotifyPrimaryTrackFormatChanged(int mediaChunkReadIndex) {
        BaseMediaChunk currentChunk = this.mediaChunks.get(mediaChunkReadIndex);
        Format trackFormat = currentChunk.trackFormat;
        if (!trackFormat.equals(this.primaryDownstreamTrackFormat)) {
            this.eventDispatcher.downstreamFormatChanged(this.primaryTrackType, trackFormat, currentChunk.trackSelectionReason, currentChunk.trackSelectionData, currentChunk.startTimeUs);
        }
        this.primaryDownstreamTrackFormat = trackFormat;
    }

    private int primaryStreamIndexToMediaChunkIndex(int primaryStreamIndex, int minChunkIndex) {
        for (int i = minChunkIndex + 1; i < this.mediaChunks.size(); i++) {
            if (this.mediaChunks.get(i).getFirstSampleIndex(0) > primaryStreamIndex) {
                return i - 1;
            }
        }
        return this.mediaChunks.size() - 1;
    }

    private BaseMediaChunk getLastMediaChunk() {
        return this.mediaChunks.get(this.mediaChunks.size() - 1);
    }

    private BaseMediaChunk discardUpstreamMediaChunksFromIndex(int chunkIndex) {
        BaseMediaChunk firstRemovedChunk = this.mediaChunks.get(chunkIndex);
        Util.removeRange(this.mediaChunks, chunkIndex, this.mediaChunks.size());
        this.primarySampleQueue.discardUpstreamSamples(firstRemovedChunk.getFirstSampleIndex(0));
        for (int i = 0; i < this.embeddedSampleQueues.length; i++) {
            this.embeddedSampleQueues[i].discardUpstreamSamples(firstRemovedChunk.getFirstSampleIndex(i + 1));
        }
        return firstRemovedChunk;
    }

    /* loaded from: classes.dex */
    public final class EmbeddedSampleStream implements SampleStream {
        private boolean formatNotificationSent;
        private final int index;
        public final ChunkSampleStream<T> parent;
        private final SampleQueue sampleQueue;

        public EmbeddedSampleStream(ChunkSampleStream<T> parent, SampleQueue sampleQueue, int index) {
            this.parent = parent;
            this.sampleQueue = sampleQueue;
            this.index = index;
        }

        @Override // com.google.android.exoplayer2.source.SampleStream
        public boolean isReady() {
            return ChunkSampleStream.this.loadingFinished || (!ChunkSampleStream.this.isPendingReset() && this.sampleQueue.hasNextSample());
        }

        @Override // com.google.android.exoplayer2.source.SampleStream
        public int skipData(long positionUs) {
            int skipCount;
            if (ChunkSampleStream.this.loadingFinished && positionUs > this.sampleQueue.getLargestQueuedTimestampUs()) {
                skipCount = this.sampleQueue.advanceToEnd();
            } else {
                skipCount = this.sampleQueue.advanceTo(positionUs, true, true);
                if (skipCount == -1) {
                    skipCount = 0;
                }
            }
            if (skipCount > 0) {
                maybeNotifyTrackFormatChanged();
            }
            return skipCount;
        }

        @Override // com.google.android.exoplayer2.source.SampleStream
        public void maybeThrowError() throws IOException {
        }

        @Override // com.google.android.exoplayer2.source.SampleStream
        public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean formatRequired) {
            if (ChunkSampleStream.this.isPendingReset()) {
                return -3;
            }
            int result = this.sampleQueue.read(formatHolder, buffer, formatRequired, ChunkSampleStream.this.loadingFinished, ChunkSampleStream.this.decodeOnlyUntilPositionUs);
            if (result == -4) {
                maybeNotifyTrackFormatChanged();
                return result;
            }
            return result;
        }

        public void release() {
            Assertions.checkState(ChunkSampleStream.this.embeddedTracksSelected[this.index]);
            ChunkSampleStream.this.embeddedTracksSelected[this.index] = false;
        }

        private void maybeNotifyTrackFormatChanged() {
            if (!this.formatNotificationSent) {
                ChunkSampleStream.this.eventDispatcher.downstreamFormatChanged(ChunkSampleStream.this.embeddedTrackTypes[this.index], ChunkSampleStream.this.embeddedTrackFormats[this.index], 0, null, ChunkSampleStream.this.lastSeekPositionUs);
                this.formatNotificationSent = true;
            }
        }
    }
}
