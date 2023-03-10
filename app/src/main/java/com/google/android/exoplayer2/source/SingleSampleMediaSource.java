package com.google.android.exoplayer2.source;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;

/* loaded from: classes.dex */
public final class SingleSampleMediaSource extends BaseMediaSource {
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;
    private final DataSource.Factory dataSourceFactory;
    private final DataSpec dataSpec;
    private final long durationUs;
    private final Format format;
    private final int minLoadableRetryCount;
    private final Timeline timeline;
    private final boolean treatLoadErrorsAsEndOfStream;

    @Deprecated
    /* loaded from: classes.dex */
    public interface EventListener {
        void onLoadError(int i, IOException iOException);
    }

    /* loaded from: classes.dex */
    public static final class Factory {
        private final DataSource.Factory dataSourceFactory;
        private boolean isCreateCalled;
        private int minLoadableRetryCount = 3;
        @Nullable
        private Object tag;
        private boolean treatLoadErrorsAsEndOfStream;

        public Factory(DataSource.Factory dataSourceFactory) {
            this.dataSourceFactory = (DataSource.Factory) Assertions.checkNotNull(dataSourceFactory);
        }

        public Factory setTag(Object tag) {
            Assertions.checkState(!this.isCreateCalled);
            this.tag = tag;
            return this;
        }

        public Factory setMinLoadableRetryCount(int minLoadableRetryCount) {
            Assertions.checkState(!this.isCreateCalled);
            this.minLoadableRetryCount = minLoadableRetryCount;
            return this;
        }

        public Factory setTreatLoadErrorsAsEndOfStream(boolean treatLoadErrorsAsEndOfStream) {
            Assertions.checkState(!this.isCreateCalled);
            this.treatLoadErrorsAsEndOfStream = treatLoadErrorsAsEndOfStream;
            return this;
        }

        public SingleSampleMediaSource createMediaSource(Uri uri, Format format, long durationUs) {
            this.isCreateCalled = true;
            return new SingleSampleMediaSource(uri, this.dataSourceFactory, format, durationUs, this.minLoadableRetryCount, this.treatLoadErrorsAsEndOfStream, this.tag);
        }

        @Deprecated
        public SingleSampleMediaSource createMediaSource(Uri uri, Format format, long durationUs, @Nullable Handler eventHandler, @Nullable MediaSourceEventListener eventListener) {
            SingleSampleMediaSource mediaSource = createMediaSource(uri, format, durationUs);
            if (eventHandler != null && eventListener != null) {
                mediaSource.addEventListener(eventHandler, eventListener);
            }
            return mediaSource;
        }
    }

    @Deprecated
    public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format, long durationUs) {
        this(uri, dataSourceFactory, format, durationUs, 3);
    }

    @Deprecated
    public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format, long durationUs, int minLoadableRetryCount) {
        this(uri, dataSourceFactory, format, durationUs, minLoadableRetryCount, false, null);
    }

    @Deprecated
    public SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format, long durationUs, int minLoadableRetryCount, Handler eventHandler, EventListener eventListener, int eventSourceId, boolean treatLoadErrorsAsEndOfStream) {
        this(uri, dataSourceFactory, format, durationUs, minLoadableRetryCount, treatLoadErrorsAsEndOfStream, null);
        if (eventHandler != null && eventListener != null) {
            addEventListener(eventHandler, new EventListenerWrapper(eventListener, eventSourceId));
        }
    }

    private SingleSampleMediaSource(Uri uri, DataSource.Factory dataSourceFactory, Format format, long durationUs, int minLoadableRetryCount, boolean treatLoadErrorsAsEndOfStream, @Nullable Object tag) {
        this.dataSourceFactory = dataSourceFactory;
        this.format = format;
        this.durationUs = durationUs;
        this.minLoadableRetryCount = minLoadableRetryCount;
        this.treatLoadErrorsAsEndOfStream = treatLoadErrorsAsEndOfStream;
        this.dataSpec = new DataSpec(uri);
        this.timeline = new SinglePeriodTimeline(durationUs, true, false, tag);
    }

    @Override // com.google.android.exoplayer2.source.BaseMediaSource
    public void prepareSourceInternal(ExoPlayer player, boolean isTopLevelSource) {
        refreshSourceInfo(this.timeline, null);
    }

    @Override // com.google.android.exoplayer2.source.MediaSource
    public void maybeThrowSourceInfoRefreshError() throws IOException {
    }

    @Override // com.google.android.exoplayer2.source.MediaSource
    public MediaPeriod createPeriod(MediaSource.MediaPeriodId id, Allocator allocator) {
        Assertions.checkArgument(id.periodIndex == 0);
        return new SingleSampleMediaPeriod(this.dataSpec, this.dataSourceFactory, this.format, this.durationUs, this.minLoadableRetryCount, createEventDispatcher(id), this.treatLoadErrorsAsEndOfStream);
    }

    @Override // com.google.android.exoplayer2.source.MediaSource
    public void releasePeriod(MediaPeriod mediaPeriod) {
        ((SingleSampleMediaPeriod) mediaPeriod).release();
    }

    @Override // com.google.android.exoplayer2.source.BaseMediaSource
    public void releaseSourceInternal() {
    }

    /* loaded from: classes.dex */
    private static final class EventListenerWrapper extends DefaultMediaSourceEventListener {
        private final EventListener eventListener;
        private final int eventSourceId;

        public EventListenerWrapper(EventListener eventListener, int eventSourceId) {
            this.eventListener = (EventListener) Assertions.checkNotNull(eventListener);
            this.eventSourceId = eventSourceId;
        }

        @Override // com.google.android.exoplayer2.source.DefaultMediaSourceEventListener, com.google.android.exoplayer2.source.MediaSourceEventListener
        public void onLoadError(int windowIndex, @Nullable MediaSource.MediaPeriodId mediaPeriodId, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
            this.eventListener.onLoadError(this.eventSourceId, error);
        }
    }
}
