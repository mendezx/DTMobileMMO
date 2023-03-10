package com.google.android.exoplayer2.extractor.mp3;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.GaplessInfoHolder;
import com.google.android.exoplayer2.extractor.Id3Peeker;
import com.google.android.exoplayer2.extractor.MpegAudioHeader;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import com.ssjj.fnsdk.core.TLog;
import java.io.EOFException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* loaded from: classes.dex */
public final class Mp3Extractor implements Extractor {
    public static final int FLAG_DISABLE_ID3_METADATA = 2;
    public static final int FLAG_ENABLE_CONSTANT_BITRATE_SEEKING = 1;
    private static final int MAX_SNIFF_BYTES = 16384;
    private static final int MAX_SYNC_BYTES = 131072;
    private static final int MPEG_AUDIO_HEADER_MASK = -128000;
    private static final int SCRATCH_LENGTH = 10;
    private static final int SEEK_HEADER_UNSET = 0;
    private long basisTimeUs;
    private ExtractorOutput extractorOutput;
    private final int flags;
    private final long forcedFirstSampleTimestampUs;
    private final GaplessInfoHolder gaplessInfoHolder;
    private final Id3Peeker id3Peeker;
    private Metadata metadata;
    private int sampleBytesRemaining;
    private long samplesRead;
    private final ParsableByteArray scratch;
    private Seeker seeker;
    private final MpegAudioHeader synchronizedHeader;
    private int synchronizedHeaderData;
    private TrackOutput trackOutput;
    public static final ExtractorsFactory FACTORY = new ExtractorsFactory() { // from class: com.google.android.exoplayer2.extractor.mp3.Mp3Extractor.1
        @Override // com.google.android.exoplayer2.extractor.ExtractorsFactory
        public Extractor[] createExtractors() {
            return new Extractor[]{new Mp3Extractor()};
        }
    };
    private static final int SEEK_HEADER_XING = Util.getIntegerCodeForString("Xing");
    private static final int SEEK_HEADER_INFO = Util.getIntegerCodeForString("Info");
    private static final int SEEK_HEADER_VBRI = Util.getIntegerCodeForString("VBRI");

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface Flags {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public interface Seeker extends SeekMap {
        long getTimeUs(long j);
    }

    public Mp3Extractor() {
        this(0);
    }

    public Mp3Extractor(int flags) {
        this(flags, C.TIME_UNSET);
    }

    public Mp3Extractor(int flags, long forcedFirstSampleTimestampUs) {
        this.flags = flags;
        this.forcedFirstSampleTimestampUs = forcedFirstSampleTimestampUs;
        this.scratch = new ParsableByteArray(10);
        this.synchronizedHeader = new MpegAudioHeader();
        this.gaplessInfoHolder = new GaplessInfoHolder();
        this.basisTimeUs = C.TIME_UNSET;
        this.id3Peeker = new Id3Peeker();
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
        return synchronize(input, true);
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void init(ExtractorOutput output) {
        this.extractorOutput = output;
        this.trackOutput = this.extractorOutput.track(0, 1);
        this.extractorOutput.endTracks();
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void seek(long position, long timeUs) {
        this.synchronizedHeaderData = 0;
        this.basisTimeUs = C.TIME_UNSET;
        this.samplesRead = 0L;
        this.sampleBytesRemaining = 0;
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void release() {
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        if (this.synchronizedHeaderData == 0) {
            try {
                synchronize(input, false);
            } catch (EOFException e) {
                return -1;
            }
        }
        if (this.seeker == null) {
            this.seeker = maybeReadSeekFrame(input);
            if (this.seeker == null || (!this.seeker.isSeekable() && (this.flags & 1) != 0)) {
                this.seeker = getConstantBitrateSeeker(input);
            }
            this.extractorOutput.seekMap(this.seeker);
            this.trackOutput.format(Format.createAudioSampleFormat(null, this.synchronizedHeader.mimeType, null, -1, 4096, this.synchronizedHeader.channels, this.synchronizedHeader.sampleRate, -1, this.gaplessInfoHolder.encoderDelay, this.gaplessInfoHolder.encoderPadding, null, null, 0, null, (this.flags & 2) != 0 ? null : this.metadata));
        }
        return readSample(input);
    }

    private int readSample(ExtractorInput extractorInput) throws IOException, InterruptedException {
        if (this.sampleBytesRemaining == 0) {
            extractorInput.resetPeekPosition();
            if (!extractorInput.peekFully(this.scratch.data, 0, 4, true)) {
                return -1;
            }
            this.scratch.setPosition(0);
            int sampleHeaderData = this.scratch.readInt();
            if (!headersMatch(sampleHeaderData, this.synchronizedHeaderData) || MpegAudioHeader.getFrameSize(sampleHeaderData) == -1) {
                extractorInput.skipFully(1);
                this.synchronizedHeaderData = 0;
                return 0;
            }
            MpegAudioHeader.populateHeader(sampleHeaderData, this.synchronizedHeader);
            if (this.basisTimeUs == C.TIME_UNSET) {
                this.basisTimeUs = this.seeker.getTimeUs(extractorInput.getPosition());
                if (this.forcedFirstSampleTimestampUs != C.TIME_UNSET) {
                    long embeddedFirstSampleTimestampUs = this.seeker.getTimeUs(0L);
                    this.basisTimeUs += this.forcedFirstSampleTimestampUs - embeddedFirstSampleTimestampUs;
                }
            }
            this.sampleBytesRemaining = this.synchronizedHeader.frameSize;
        }
        int bytesAppended = this.trackOutput.sampleData(extractorInput, this.sampleBytesRemaining, true);
        if (bytesAppended == -1) {
            return -1;
        }
        this.sampleBytesRemaining -= bytesAppended;
        if (this.sampleBytesRemaining > 0) {
            return 0;
        }
        long timeUs = this.basisTimeUs + ((this.samplesRead * 1000000) / this.synchronizedHeader.sampleRate);
        this.trackOutput.sampleMetadata(timeUs, 1, this.synchronizedHeader.frameSize, 0, null);
        this.samplesRead += this.synchronizedHeader.samplesPerFrame;
        this.sampleBytesRemaining = 0;
        return 0;
    }

    /* JADX WARN: Code restructure failed: missing block: B:23:0x004c, code lost:
        if (r16 == false) goto L63;
     */
    /* JADX WARN: Code restructure failed: missing block: B:24:0x004e, code lost:
        r15.skipFully(r5 + r7);
     */
    /* JADX WARN: Code restructure failed: missing block: B:25:0x0053, code lost:
        r14.synchronizedHeaderData = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:26:0x0056, code lost:
        return true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x00b6, code lost:
        r15.resetPeekPosition();
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean synchronize(com.google.android.exoplayer2.extractor.ExtractorInput r15, boolean r16) throws java.io.IOException, java.lang.InterruptedException {
        /*
            r14 = this;
            r9 = 0
            r0 = 0
            r5 = 0
            r7 = 0
            if (r16 == 0) goto L57
            r6 = 16384(0x4000, float:2.2959E-41)
        L8:
            r15.resetPeekPosition()
            long r10 = r15.getPosition()
            r12 = 0
            int r10 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1))
            if (r10 != 0) goto L3d
            int r10 = r14.flags
            r10 = r10 & 2
            if (r10 == 0) goto L5a
            r4 = 1
        L1c:
            if (r4 == 0) goto L5c
            com.google.android.exoplayer2.metadata.id3.Id3Decoder$FramePredicate r3 = com.google.android.exoplayer2.extractor.GaplessInfoHolder.GAPLESS_INFO_ID3_FRAME_PREDICATE
        L20:
            com.google.android.exoplayer2.extractor.Id3Peeker r10 = r14.id3Peeker
            com.google.android.exoplayer2.metadata.Metadata r10 = r10.peekId3Data(r15, r3)
            r14.metadata = r10
            com.google.android.exoplayer2.metadata.Metadata r10 = r14.metadata
            if (r10 == 0) goto L33
            com.google.android.exoplayer2.extractor.GaplessInfoHolder r10 = r14.gaplessInfoHolder
            com.google.android.exoplayer2.metadata.Metadata r11 = r14.metadata
            r10.setFromMetadata(r11)
        L33:
            long r10 = r15.getPeekPosition()
            int r5 = (int) r10
            if (r16 != 0) goto L3d
            r15.skipFully(r5)
        L3d:
            com.google.android.exoplayer2.util.ParsableByteArray r10 = r14.scratch
            byte[] r11 = r10.data
            r12 = 0
            r13 = 4
            if (r9 <= 0) goto L5e
            r10 = 1
        L46:
            boolean r10 = r15.peekFully(r11, r12, r13, r10)
            if (r10 != 0) goto L60
        L4c:
            if (r16 == 0) goto Lb6
            int r10 = r5 + r7
            r15.skipFully(r10)
        L53:
            r14.synchronizedHeaderData = r0
            r10 = 1
        L56:
            return r10
        L57:
            r6 = 131072(0x20000, float:1.83671E-40)
            goto L8
        L5a:
            r4 = 0
            goto L1c
        L5c:
            r3 = 0
            goto L20
        L5e:
            r10 = 0
            goto L46
        L60:
            com.google.android.exoplayer2.util.ParsableByteArray r10 = r14.scratch
            r11 = 0
            r10.setPosition(r11)
            com.google.android.exoplayer2.util.ParsableByteArray r10 = r14.scratch
            int r2 = r10.readInt()
            if (r0 == 0) goto L75
            long r10 = (long) r0
            boolean r10 = headersMatch(r2, r10)
            if (r10 == 0) goto L7c
        L75:
            int r1 = com.google.android.exoplayer2.extractor.MpegAudioHeader.getFrameSize(r2)
            r10 = -1
            if (r1 != r10) goto La1
        L7c:
            int r8 = r7 + 1
            if (r7 != r6) goto L8d
            if (r16 != 0) goto L8a
            com.google.android.exoplayer2.ParserException r10 = new com.google.android.exoplayer2.ParserException
            java.lang.String r11 = "Searched too many bytes."
            r10.<init>(r11)
            throw r10
        L8a:
            r10 = 0
            r7 = r8
            goto L56
        L8d:
            r9 = 0
            r0 = 0
            if (r16 == 0) goto L9b
            r15.resetPeekPosition()
            int r10 = r5 + r8
            r15.advancePeekPosition(r10)
            r7 = r8
            goto L3d
        L9b:
            r10 = 1
            r15.skipFully(r10)
            r7 = r8
            goto L3d
        La1:
            int r9 = r9 + 1
            r10 = 1
            if (r9 != r10) goto Lb2
            com.google.android.exoplayer2.extractor.MpegAudioHeader r10 = r14.synchronizedHeader
            com.google.android.exoplayer2.extractor.MpegAudioHeader.populateHeader(r2, r10)
            r0 = r2
        Lac:
            int r10 = r1 + (-4)
            r15.advancePeekPosition(r10)
            goto L3d
        Lb2:
            r10 = 4
            if (r9 != r10) goto Lac
            goto L4c
        Lb6:
            r15.resetPeekPosition()
            goto L53
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer2.extractor.mp3.Mp3Extractor.synchronize(com.google.android.exoplayer2.extractor.ExtractorInput, boolean):boolean");
    }

    private Seeker maybeReadSeekFrame(ExtractorInput input) throws IOException, InterruptedException {
        Seeker seeker;
        int xingBase = 21;
        ParsableByteArray frame = new ParsableByteArray(this.synchronizedHeader.frameSize);
        input.peekFully(frame.data, 0, this.synchronizedHeader.frameSize);
        if ((this.synchronizedHeader.version & 1) != 0) {
            if (this.synchronizedHeader.channels != 1) {
                xingBase = 36;
            }
        } else if (this.synchronizedHeader.channels == 1) {
            xingBase = 13;
        }
        int seekHeader = getSeekFrameHeader(frame, xingBase);
        if (seekHeader == SEEK_HEADER_XING || seekHeader == SEEK_HEADER_INFO) {
            seeker = XingSeeker.create(input.getLength(), input.getPosition(), this.synchronizedHeader, frame);
            if (seeker != null && !this.gaplessInfoHolder.hasGaplessInfo()) {
                input.resetPeekPosition();
                input.advancePeekPosition(xingBase + TLog.C141);
                input.peekFully(this.scratch.data, 0, 3);
                this.scratch.setPosition(0);
                this.gaplessInfoHolder.setFromXingHeaderValue(this.scratch.readUnsignedInt24());
            }
            input.skipFully(this.synchronizedHeader.frameSize);
            if (seeker != null && !seeker.isSeekable() && seekHeader == SEEK_HEADER_INFO) {
                return getConstantBitrateSeeker(input);
            }
        } else if (seekHeader == SEEK_HEADER_VBRI) {
            seeker = VbriSeeker.create(input.getLength(), input.getPosition(), this.synchronizedHeader, frame);
            input.skipFully(this.synchronizedHeader.frameSize);
        } else {
            seeker = null;
            input.resetPeekPosition();
        }
        return seeker;
    }

    private Seeker getConstantBitrateSeeker(ExtractorInput input) throws IOException, InterruptedException {
        input.peekFully(this.scratch.data, 0, 4);
        this.scratch.setPosition(0);
        MpegAudioHeader.populateHeader(this.scratch.readInt(), this.synchronizedHeader);
        return new ConstantBitrateSeeker(input.getLength(), input.getPosition(), this.synchronizedHeader);
    }

    private static boolean headersMatch(int headerA, long headerB) {
        return ((long) (MPEG_AUDIO_HEADER_MASK & headerA)) == ((-128000) & headerB);
    }

    private static int getSeekFrameHeader(ParsableByteArray frame, int xingBase) {
        if (frame.limit() >= xingBase + 4) {
            frame.setPosition(xingBase);
            int headerData = frame.readInt();
            if (headerData == SEEK_HEADER_XING || headerData == SEEK_HEADER_INFO) {
                return headerData;
            }
        }
        if (frame.limit() >= 40) {
            frame.setPosition(36);
            if (frame.readInt() == SEEK_HEADER_VBRI) {
                return SEEK_HEADER_VBRI;
            }
        }
        return 0;
    }
}
