package com.google.android.exoplayer2.source.hls;

import android.util.Pair;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.extractor.DefaultExtractorInput;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.Id3Decoder;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.google.android.exoplayer2.source.chunk.MediaChunk;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class HlsMediaChunk extends MediaChunk {
    private static final String PRIV_TIMESTAMP_FRAME_OWNER = "com.apple.streaming.transportStreamTimestamp";
    private static final AtomicInteger uidSource = new AtomicInteger();
    private int bytesLoaded;
    public final int discontinuitySequenceNumber;
    private final Extractor extractor;
    private final boolean hasGapTag;
    public final HlsMasterPlaylist.HlsUrl hlsUrl;
    private final ParsableByteArray id3Data;
    private final Id3Decoder id3Decoder;
    private boolean id3TimestampPeeked;
    private final DataSource initDataSource;
    private final DataSpec initDataSpec;
    private boolean initLoadCompleted;
    private int initSegmentBytesLoaded;
    private final boolean isEncrypted;
    private final boolean isMasterTimestampSource;
    private final boolean isPackedAudioExtractor;
    private volatile boolean loadCanceled;
    private volatile boolean loadCompleted;
    private HlsSampleStreamWrapper output;
    private final boolean reusingExtractor;
    private final boolean shouldSpliceIn;
    private final TimestampAdjuster timestampAdjuster;
    public final int uid;

    public HlsMediaChunk(HlsExtractorFactory extractorFactory, DataSource dataSource, DataSpec dataSpec, DataSpec initDataSpec, HlsMasterPlaylist.HlsUrl hlsUrl, List<Format> muxedCaptionFormats, int trackSelectionReason, Object trackSelectionData, long startTimeUs, long endTimeUs, long chunkMediaSequence, int discontinuitySequenceNumber, boolean hasGapTag, boolean isMasterTimestampSource, TimestampAdjuster timestampAdjuster, HlsMediaChunk previousChunk, DrmInitData drmInitData, byte[] fullSegmentEncryptionKey, byte[] encryptionIv) {
        super(buildDataSource(dataSource, fullSegmentEncryptionKey, encryptionIv), dataSpec, hlsUrl.format, trackSelectionReason, trackSelectionData, startTimeUs, endTimeUs, chunkMediaSequence);
        this.discontinuitySequenceNumber = discontinuitySequenceNumber;
        this.initDataSpec = initDataSpec;
        this.hlsUrl = hlsUrl;
        this.isMasterTimestampSource = isMasterTimestampSource;
        this.timestampAdjuster = timestampAdjuster;
        this.isEncrypted = this.dataSource instanceof Aes128DataSource;
        this.hasGapTag = hasGapTag;
        Extractor previousExtractor = null;
        if (previousChunk != null) {
            this.shouldSpliceIn = previousChunk.hlsUrl != hlsUrl;
            previousExtractor = (previousChunk.discontinuitySequenceNumber != discontinuitySequenceNumber || this.shouldSpliceIn) ? null : previousChunk.extractor;
        } else {
            this.shouldSpliceIn = false;
        }
        Pair<Extractor, Boolean> extractorData = extractorFactory.createExtractor(previousExtractor, dataSpec.uri, this.trackFormat, muxedCaptionFormats, drmInitData, timestampAdjuster);
        this.extractor = (Extractor) extractorData.first;
        this.isPackedAudioExtractor = ((Boolean) extractorData.second).booleanValue();
        this.reusingExtractor = this.extractor == previousExtractor;
        this.initLoadCompleted = this.reusingExtractor && initDataSpec != null;
        if (this.isPackedAudioExtractor) {
            if (previousChunk != null && previousChunk.id3Data != null) {
                this.id3Decoder = previousChunk.id3Decoder;
                this.id3Data = previousChunk.id3Data;
            } else {
                this.id3Decoder = new Id3Decoder();
                this.id3Data = new ParsableByteArray(10);
            }
        } else {
            this.id3Decoder = null;
            this.id3Data = null;
        }
        this.initDataSource = dataSource;
        this.uid = uidSource.getAndIncrement();
    }

    public void init(HlsSampleStreamWrapper output) {
        this.output = output;
        output.init(this.uid, this.shouldSpliceIn, this.reusingExtractor);
        if (!this.reusingExtractor) {
            this.extractor.init(output);
        }
    }

    @Override // com.google.android.exoplayer2.source.chunk.MediaChunk
    public boolean isLoadCompleted() {
        return this.loadCompleted;
    }

    @Override // com.google.android.exoplayer2.source.chunk.Chunk
    public long bytesLoaded() {
        return this.bytesLoaded;
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.Loadable
    public void cancelLoad() {
        this.loadCanceled = true;
    }

    @Override // com.google.android.exoplayer2.upstream.Loader.Loadable
    public void load() throws IOException, InterruptedException {
        maybeLoadInitData();
        if (!this.loadCanceled) {
            if (!this.hasGapTag) {
                loadMedia();
            }
            this.loadCompleted = true;
        }
    }

    private void maybeLoadInitData() throws IOException, InterruptedException {
        if (!this.initLoadCompleted && this.initDataSpec != null) {
            DataSpec initSegmentDataSpec = this.initDataSpec.subrange(this.initSegmentBytesLoaded);
            try {
                ExtractorInput input = new DefaultExtractorInput(this.initDataSource, initSegmentDataSpec.absoluteStreamPosition, this.initDataSource.open(initSegmentDataSpec));
                int result = 0;
                while (result == 0 && !this.loadCanceled) {
                    result = this.extractor.read(input, null);
                }
                this.initSegmentBytesLoaded = (int) (input.getPosition() - this.initDataSpec.absoluteStreamPosition);
                Util.closeQuietly(this.initDataSource);
                this.initLoadCompleted = true;
            } catch (Throwable th) {
                Util.closeQuietly(this.initDataSource);
                throw th;
            }
        }
    }

    private void loadMedia() throws IOException, InterruptedException {
        DataSpec loadDataSpec;
        boolean skipLoadedBytes = true;
        if (this.isEncrypted) {
            loadDataSpec = this.dataSpec;
            if (this.bytesLoaded == 0) {
                skipLoadedBytes = false;
            }
        } else {
            loadDataSpec = this.dataSpec.subrange(this.bytesLoaded);
            skipLoadedBytes = false;
        }
        if (!this.isMasterTimestampSource) {
            this.timestampAdjuster.waitUntilInitialized();
        } else if (this.timestampAdjuster.getFirstSampleTimestampUs() == Long.MAX_VALUE) {
            this.timestampAdjuster.setFirstSampleTimestampUs(this.startTimeUs);
        }
        try {
            ExtractorInput input = new DefaultExtractorInput(this.dataSource, loadDataSpec.absoluteStreamPosition, this.dataSource.open(loadDataSpec));
            if (this.isPackedAudioExtractor && !this.id3TimestampPeeked) {
                long id3Timestamp = peekId3PrivTimestamp(input);
                this.id3TimestampPeeked = true;
                this.output.setSampleOffsetUs(id3Timestamp != C.TIME_UNSET ? this.timestampAdjuster.adjustTsTimestamp(id3Timestamp) : this.startTimeUs);
            }
            if (skipLoadedBytes) {
                input.skipFully(this.bytesLoaded);
            }
            int result = 0;
            while (result == 0 && !this.loadCanceled) {
                result = this.extractor.read(input, null);
            }
            this.bytesLoaded = (int) (input.getPosition() - this.dataSpec.absoluteStreamPosition);
        } finally {
            Util.closeQuietly(this.dataSource);
        }
    }

    private long peekId3PrivTimestamp(ExtractorInput input) throws IOException, InterruptedException {
        input.resetPeekPosition();
        if (!input.peekFully(this.id3Data.data, 0, 10, true)) {
            return C.TIME_UNSET;
        }
        this.id3Data.reset(10);
        int id = this.id3Data.readUnsignedInt24();
        if (id != Id3Decoder.ID3_TAG) {
            return C.TIME_UNSET;
        }
        this.id3Data.skipBytes(3);
        int id3Size = this.id3Data.readSynchSafeInt();
        int requiredCapacity = id3Size + 10;
        if (requiredCapacity > this.id3Data.capacity()) {
            byte[] data = this.id3Data.data;
            this.id3Data.reset(requiredCapacity);
            System.arraycopy(data, 0, this.id3Data.data, 0, 10);
        }
        if (!input.peekFully(this.id3Data.data, 10, id3Size, true)) {
            return C.TIME_UNSET;
        }
        Metadata metadata = this.id3Decoder.decode(this.id3Data.data, id3Size);
        if (metadata == null) {
            return C.TIME_UNSET;
        }
        int metadataLength = metadata.length();
        for (int i = 0; i < metadataLength; i++) {
            Metadata.Entry frame = metadata.get(i);
            if (frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) frame;
                if (PRIV_TIMESTAMP_FRAME_OWNER.equals(privFrame.owner)) {
                    System.arraycopy(privFrame.privateData, 0, this.id3Data.data, 0, 8);
                    this.id3Data.reset(8);
                    return this.id3Data.readLong() & 8589934591L;
                }
            }
        }
        return C.TIME_UNSET;
    }

    private static DataSource buildDataSource(DataSource dataSource, byte[] fullSegmentEncryptionKey, byte[] encryptionIv) {
        if (fullSegmentEncryptionKey != null) {
            return new Aes128DataSource(dataSource, fullSegmentEncryptionKey, encryptionIv);
        }
        return dataSource;
    }
}
