package com.google.android.exoplayer2.extractor.ogg;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.util.ParsableByteArray;
import java.io.IOException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class StreamReader {
    private static final int STATE_END_OF_INPUT = 3;
    private static final int STATE_READ_HEADERS = 0;
    private static final int STATE_READ_PAYLOAD = 2;
    private static final int STATE_SKIP_HEADERS = 1;
    private long currentGranule;
    private ExtractorOutput extractorOutput;
    private boolean formatSet;
    private long lengthOfReadPacket;
    private final OggPacket oggPacket = new OggPacket();
    private OggSeeker oggSeeker;
    private long payloadStartPosition;
    private int sampleRate;
    private boolean seekMapSet;
    private SetupData setupData;
    private int state;
    private long targetGranule;
    private TrackOutput trackOutput;

    protected abstract long preparePayload(ParsableByteArray parsableByteArray);

    protected abstract boolean readHeaders(ParsableByteArray parsableByteArray, long j, SetupData setupData) throws IOException, InterruptedException;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class SetupData {
        Format format;
        OggSeeker oggSeeker;

        SetupData() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void init(ExtractorOutput output, TrackOutput trackOutput) {
        this.extractorOutput = output;
        this.trackOutput = trackOutput;
        reset(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void reset(boolean headerData) {
        if (headerData) {
            this.setupData = new SetupData();
            this.payloadStartPosition = 0L;
            this.state = 0;
        } else {
            this.state = 1;
        }
        this.targetGranule = -1L;
        this.currentGranule = 0L;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void seek(long position, long timeUs) {
        this.oggPacket.reset();
        if (position == 0) {
            reset(!this.seekMapSet);
        } else if (this.state != 0) {
            this.targetGranule = this.oggSeeker.startSeek(timeUs);
            this.state = 2;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        switch (this.state) {
            case 0:
                return readHeaders(input);
            case 1:
                input.skipFully((int) this.payloadStartPosition);
                this.state = 2;
                return 0;
            case 2:
                return readPayload(input, seekPosition);
            default:
                throw new IllegalStateException();
        }
    }

    private int readHeaders(ExtractorInput input) throws IOException, InterruptedException {
        boolean readingHeaders = true;
        while (readingHeaders) {
            if (!this.oggPacket.populate(input)) {
                this.state = 3;
                return -1;
            }
            this.lengthOfReadPacket = input.getPosition() - this.payloadStartPosition;
            readingHeaders = readHeaders(this.oggPacket.getPayload(), this.payloadStartPosition, this.setupData);
            if (readingHeaders) {
                this.payloadStartPosition = input.getPosition();
            }
        }
        this.sampleRate = this.setupData.format.sampleRate;
        if (!this.formatSet) {
            this.trackOutput.format(this.setupData.format);
            this.formatSet = true;
        }
        if (this.setupData.oggSeeker != null) {
            this.oggSeeker = this.setupData.oggSeeker;
        } else if (input.getLength() == -1) {
            this.oggSeeker = new UnseekableOggSeeker();
        } else {
            OggPageHeader firstPayloadPageHeader = this.oggPacket.getPageHeader();
            this.oggSeeker = new DefaultOggSeeker(this.payloadStartPosition, input.getLength(), this, firstPayloadPageHeader.bodySize + firstPayloadPageHeader.headerSize, firstPayloadPageHeader.granulePosition);
        }
        this.setupData = null;
        this.state = 2;
        this.oggPacket.trimPayload();
        return 0;
    }

    private int readPayload(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        long position = this.oggSeeker.read(input);
        if (position >= 0) {
            seekPosition.position = position;
            return 1;
        }
        if (position < -1) {
            onSeekEnd(-(2 + position));
        }
        if (!this.seekMapSet) {
            SeekMap seekMap = this.oggSeeker.createSeekMap();
            this.extractorOutput.seekMap(seekMap);
            this.seekMapSet = true;
        }
        if (this.lengthOfReadPacket > 0 || this.oggPacket.populate(input)) {
            this.lengthOfReadPacket = 0L;
            ParsableByteArray payload = this.oggPacket.getPayload();
            long granulesInPacket = preparePayload(payload);
            if (granulesInPacket >= 0 && this.currentGranule + granulesInPacket >= this.targetGranule) {
                long timeUs = convertGranuleToTime(this.currentGranule);
                this.trackOutput.sampleData(payload, payload.limit());
                this.trackOutput.sampleMetadata(timeUs, 1, payload.limit(), 0, null);
                this.targetGranule = -1L;
            }
            this.currentGranule += granulesInPacket;
            return 0;
        }
        this.state = 3;
        return -1;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long convertGranuleToTime(long granule) {
        return (1000000 * granule) / this.sampleRate;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long convertTimeToGranule(long timeUs) {
        return (this.sampleRate * timeUs) / 1000000;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onSeekEnd(long currentGranule) {
        this.currentGranule = currentGranule;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class UnseekableOggSeeker implements OggSeeker {
        private UnseekableOggSeeker() {
        }

        @Override // com.google.android.exoplayer2.extractor.ogg.OggSeeker
        public long read(ExtractorInput input) throws IOException, InterruptedException {
            return -1L;
        }

        @Override // com.google.android.exoplayer2.extractor.ogg.OggSeeker
        public long startSeek(long timeUs) {
            return 0L;
        }

        @Override // com.google.android.exoplayer2.extractor.ogg.OggSeeker
        public SeekMap createSeekMap() {
            return new SeekMap.Unseekable(C.TIME_UNSET);
        }
    }
}
