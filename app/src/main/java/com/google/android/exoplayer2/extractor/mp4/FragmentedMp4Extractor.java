package com.google.android.exoplayer2.extractor.mp4;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.extractor.ChunkIndex;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.extractor.mp4.Atom;
import com.google.android.exoplayer2.text.cea.CeaUtil;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.NalUnitUtil;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/* loaded from: classes.dex */
public final class FragmentedMp4Extractor implements Extractor {
    public static final int FLAG_ENABLE_EMSG_TRACK = 4;
    private static final int FLAG_SIDELOADED = 8;
    public static final int FLAG_WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME = 1;
    public static final int FLAG_WORKAROUND_IGNORE_EDIT_LISTS = 16;
    public static final int FLAG_WORKAROUND_IGNORE_TFDT_BOX = 2;
    private static final int STATE_READING_ATOM_HEADER = 0;
    private static final int STATE_READING_ATOM_PAYLOAD = 1;
    private static final int STATE_READING_ENCRYPTION_DATA = 2;
    private static final int STATE_READING_SAMPLE_CONTINUE = 4;
    private static final int STATE_READING_SAMPLE_START = 3;
    private static final String TAG = "FragmentedMp4Extractor";
    @Nullable
    private final TrackOutput additionalEmsgTrackOutput;
    private ParsableByteArray atomData;
    private final ParsableByteArray atomHeader;
    private int atomHeaderBytesRead;
    private long atomSize;
    private int atomType;
    private TrackOutput[] cea608TrackOutputs;
    private final List<Format> closedCaptionFormats;
    private final ArrayDeque<Atom.ContainerAtom> containerAtoms;
    private TrackBundle currentTrackBundle;
    private long durationUs;
    private TrackOutput[] emsgTrackOutputs;
    private long endOfMdatPosition;
    private final byte[] extendedTypeScratch;
    private ExtractorOutput extractorOutput;
    private final int flags;
    private boolean haveOutputSeekMap;
    private final ParsableByteArray nalBuffer;
    private final ParsableByteArray nalPrefix;
    private final ParsableByteArray nalStartCode;
    private int parserState;
    private int pendingMetadataSampleBytes;
    private final ArrayDeque<MetadataSampleInfo> pendingMetadataSampleInfos;
    private long pendingSeekTimeUs;
    private boolean processSeiNalUnitPayload;
    private int sampleBytesWritten;
    private int sampleCurrentNalBytesRemaining;
    private int sampleSize;
    private long segmentIndexEarliestPresentationTimeUs;
    @Nullable
    private final DrmInitData sideloadedDrmInitData;
    @Nullable
    private final Track sideloadedTrack;
    @Nullable
    private final TimestampAdjuster timestampAdjuster;
    private final SparseArray<TrackBundle> trackBundles;
    public static final ExtractorsFactory FACTORY = new ExtractorsFactory() { // from class: com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor.1
        @Override // com.google.android.exoplayer2.extractor.ExtractorsFactory
        public Extractor[] createExtractors() {
            return new Extractor[]{new FragmentedMp4Extractor()};
        }
    };
    private static final int SAMPLE_GROUP_TYPE_seig = Util.getIntegerCodeForString("seig");
    private static final byte[] PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE = {-94, 57, 79, 82, 90, -101, 79, 20, -94, 68, 108, 66, 124, 100, -115, -12};
    private static final Format EMSG_FORMAT = Format.createSampleFormat(null, MimeTypes.APPLICATION_EMSG, Long.MAX_VALUE);

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface Flags {
    }

    public FragmentedMp4Extractor() {
        this(0);
    }

    public FragmentedMp4Extractor(int flags) {
        this(flags, null);
    }

    public FragmentedMp4Extractor(int flags, @Nullable TimestampAdjuster timestampAdjuster) {
        this(flags, timestampAdjuster, null, null);
    }

    public FragmentedMp4Extractor(int flags, @Nullable TimestampAdjuster timestampAdjuster, @Nullable Track sideloadedTrack, @Nullable DrmInitData sideloadedDrmInitData) {
        this(flags, timestampAdjuster, sideloadedTrack, sideloadedDrmInitData, Collections.emptyList());
    }

    public FragmentedMp4Extractor(int flags, @Nullable TimestampAdjuster timestampAdjuster, @Nullable Track sideloadedTrack, @Nullable DrmInitData sideloadedDrmInitData, List<Format> closedCaptionFormats) {
        this(flags, timestampAdjuster, sideloadedTrack, sideloadedDrmInitData, closedCaptionFormats, null);
    }

    public FragmentedMp4Extractor(int flags, @Nullable TimestampAdjuster timestampAdjuster, @Nullable Track sideloadedTrack, @Nullable DrmInitData sideloadedDrmInitData, List<Format> closedCaptionFormats, @Nullable TrackOutput additionalEmsgTrackOutput) {
        this.flags = (sideloadedTrack != null ? 8 : 0) | flags;
        this.timestampAdjuster = timestampAdjuster;
        this.sideloadedTrack = sideloadedTrack;
        this.sideloadedDrmInitData = sideloadedDrmInitData;
        this.closedCaptionFormats = Collections.unmodifiableList(closedCaptionFormats);
        this.additionalEmsgTrackOutput = additionalEmsgTrackOutput;
        this.atomHeader = new ParsableByteArray(16);
        this.nalStartCode = new ParsableByteArray(NalUnitUtil.NAL_START_CODE);
        this.nalPrefix = new ParsableByteArray(5);
        this.nalBuffer = new ParsableByteArray();
        this.extendedTypeScratch = new byte[16];
        this.containerAtoms = new ArrayDeque<>();
        this.pendingMetadataSampleInfos = new ArrayDeque<>();
        this.trackBundles = new SparseArray<>();
        this.durationUs = C.TIME_UNSET;
        this.pendingSeekTimeUs = C.TIME_UNSET;
        this.segmentIndexEarliestPresentationTimeUs = C.TIME_UNSET;
        enterReadingAtomHeaderState();
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
        return Sniffer.sniffFragmented(input);
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void init(ExtractorOutput output) {
        this.extractorOutput = output;
        if (this.sideloadedTrack != null) {
            TrackBundle bundle = new TrackBundle(output.track(0, this.sideloadedTrack.type));
            bundle.init(this.sideloadedTrack, new DefaultSampleValues(0, 0, 0, 0));
            this.trackBundles.put(0, bundle);
            maybeInitExtraTracks();
            this.extractorOutput.endTracks();
        }
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void seek(long position, long timeUs) {
        int trackCount = this.trackBundles.size();
        for (int i = 0; i < trackCount; i++) {
            this.trackBundles.valueAt(i).reset();
        }
        this.pendingMetadataSampleInfos.clear();
        this.pendingMetadataSampleBytes = 0;
        this.pendingSeekTimeUs = timeUs;
        this.containerAtoms.clear();
        enterReadingAtomHeaderState();
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public void release() {
    }

    @Override // com.google.android.exoplayer2.extractor.Extractor
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        while (true) {
            switch (this.parserState) {
                case 0:
                    if (readAtomHeader(input)) {
                        break;
                    } else {
                        return -1;
                    }
                case 1:
                    readAtomPayload(input);
                    break;
                case 2:
                    readEncryptionData(input);
                    break;
                default:
                    if (!readSample(input)) {
                        break;
                    } else {
                        return 0;
                    }
            }
        }
    }

    private void enterReadingAtomHeaderState() {
        this.parserState = 0;
        this.atomHeaderBytesRead = 0;
    }

    private boolean readAtomHeader(ExtractorInput input) throws IOException, InterruptedException {
        if (this.atomHeaderBytesRead == 0) {
            if (!input.readFully(this.atomHeader.data, 0, 8, true)) {
                return false;
            }
            this.atomHeaderBytesRead = 8;
            this.atomHeader.setPosition(0);
            this.atomSize = this.atomHeader.readUnsignedInt();
            this.atomType = this.atomHeader.readInt();
        }
        if (this.atomSize == 1) {
            input.readFully(this.atomHeader.data, 8, 8);
            this.atomHeaderBytesRead += 8;
            this.atomSize = this.atomHeader.readUnsignedLongToLong();
        } else if (this.atomSize == 0) {
            long endPosition = input.getLength();
            if (endPosition == -1 && !this.containerAtoms.isEmpty()) {
                endPosition = this.containerAtoms.peek().endPosition;
            }
            if (endPosition != -1) {
                this.atomSize = (endPosition - input.getPosition()) + this.atomHeaderBytesRead;
            }
        }
        if (this.atomSize < this.atomHeaderBytesRead) {
            throw new ParserException("Atom size less than header length (unsupported).");
        }
        long atomPosition = input.getPosition() - this.atomHeaderBytesRead;
        if (this.atomType == Atom.TYPE_moof) {
            int trackCount = this.trackBundles.size();
            for (int i = 0; i < trackCount; i++) {
                TrackFragment fragment = this.trackBundles.valueAt(i).fragment;
                fragment.atomPosition = atomPosition;
                fragment.auxiliaryDataPosition = atomPosition;
                fragment.dataPosition = atomPosition;
            }
        }
        if (this.atomType == Atom.TYPE_mdat) {
            this.currentTrackBundle = null;
            this.endOfMdatPosition = this.atomSize + atomPosition;
            if (!this.haveOutputSeekMap) {
                this.extractorOutput.seekMap(new SeekMap.Unseekable(this.durationUs, atomPosition));
                this.haveOutputSeekMap = true;
            }
            this.parserState = 2;
            return true;
        }
        if (shouldParseContainerAtom(this.atomType)) {
            long endPosition2 = (input.getPosition() + this.atomSize) - 8;
            this.containerAtoms.push(new Atom.ContainerAtom(this.atomType, endPosition2));
            if (this.atomSize == this.atomHeaderBytesRead) {
                processAtomEnded(endPosition2);
            } else {
                enterReadingAtomHeaderState();
            }
        } else if (shouldParseLeafAtom(this.atomType)) {
            if (this.atomHeaderBytesRead != 8) {
                throw new ParserException("Leaf atom defines extended atom size (unsupported).");
            }
            if (this.atomSize > 2147483647L) {
                throw new ParserException("Leaf atom with length > 2147483647 (unsupported).");
            }
            this.atomData = new ParsableByteArray((int) this.atomSize);
            System.arraycopy(this.atomHeader.data, 0, this.atomData.data, 0, 8);
            this.parserState = 1;
        } else if (this.atomSize > 2147483647L) {
            throw new ParserException("Skipping atom with length > 2147483647 (unsupported).");
        } else {
            this.atomData = null;
            this.parserState = 1;
        }
        return true;
    }

    private void readAtomPayload(ExtractorInput input) throws IOException, InterruptedException {
        int atomPayloadSize = ((int) this.atomSize) - this.atomHeaderBytesRead;
        if (this.atomData != null) {
            input.readFully(this.atomData.data, 8, atomPayloadSize);
            onLeafAtomRead(new Atom.LeafAtom(this.atomType, this.atomData), input.getPosition());
        } else {
            input.skipFully(atomPayloadSize);
        }
        processAtomEnded(input.getPosition());
    }

    private void processAtomEnded(long atomEndPosition) throws ParserException {
        while (!this.containerAtoms.isEmpty() && this.containerAtoms.peek().endPosition == atomEndPosition) {
            onContainerAtomRead(this.containerAtoms.pop());
        }
        enterReadingAtomHeaderState();
    }

    private void onLeafAtomRead(Atom.LeafAtom leaf, long inputPosition) throws ParserException {
        if (!this.containerAtoms.isEmpty()) {
            this.containerAtoms.peek().add(leaf);
        } else if (leaf.type == Atom.TYPE_sidx) {
            Pair<Long, ChunkIndex> result = parseSidx(leaf.data, inputPosition);
            this.segmentIndexEarliestPresentationTimeUs = ((Long) result.first).longValue();
            this.extractorOutput.seekMap((SeekMap) result.second);
            this.haveOutputSeekMap = true;
        } else if (leaf.type == Atom.TYPE_emsg) {
            onEmsgLeafAtomRead(leaf.data);
        }
    }

    private void onContainerAtomRead(Atom.ContainerAtom container) throws ParserException {
        if (container.type == Atom.TYPE_moov) {
            onMoovContainerAtomRead(container);
        } else if (container.type == Atom.TYPE_moof) {
            onMoofContainerAtomRead(container);
        } else if (!this.containerAtoms.isEmpty()) {
            this.containerAtoms.peek().add(container);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void onMoovContainerAtomRead(Atom.ContainerAtom moov) throws ParserException {
        Assertions.checkState(this.sideloadedTrack == null, "Unexpected moov box.");
        DrmInitData drmInitData = this.sideloadedDrmInitData != null ? this.sideloadedDrmInitData : getDrmInitDataFromAtoms(moov.leafChildren);
        Atom.ContainerAtom mvex = moov.getContainerAtomOfType(Atom.TYPE_mvex);
        SparseArray sparseArray = new SparseArray();
        long duration = C.TIME_UNSET;
        int mvexChildrenSize = mvex.leafChildren.size();
        for (int i = 0; i < mvexChildrenSize; i++) {
            Atom.LeafAtom atom = mvex.leafChildren.get(i);
            if (atom.type == Atom.TYPE_trex) {
                Pair<Integer, DefaultSampleValues> trexData = parseTrex(atom.data);
                sparseArray.put(((Integer) trexData.first).intValue(), trexData.second);
            } else if (atom.type == Atom.TYPE_mehd) {
                duration = parseMehd(atom.data);
            }
        }
        SparseArray<Track> tracks = new SparseArray<>();
        int moovContainerChildrenSize = moov.containerChildren.size();
        for (int i2 = 0; i2 < moovContainerChildrenSize; i2++) {
            Atom.ContainerAtom atom2 = moov.containerChildren.get(i2);
            if (atom2.type == Atom.TYPE_trak) {
                Track track = AtomParsers.parseTrak(atom2, moov.getLeafAtomOfType(Atom.TYPE_mvhd), duration, drmInitData, (this.flags & 16) != 0, false);
                if (track != null) {
                    tracks.put(track.id, track);
                }
            }
        }
        int trackCount = tracks.size();
        if (this.trackBundles.size() == 0) {
            for (int i3 = 0; i3 < trackCount; i3++) {
                Track track2 = tracks.valueAt(i3);
                TrackBundle trackBundle = new TrackBundle(this.extractorOutput.track(i3, track2.type));
                trackBundle.init(track2, getDefaultSampleValues(sparseArray, track2.id));
                this.trackBundles.put(track2.id, trackBundle);
                this.durationUs = Math.max(this.durationUs, track2.durationUs);
            }
            maybeInitExtraTracks();
            this.extractorOutput.endTracks();
            return;
        }
        Assertions.checkState(this.trackBundles.size() == trackCount);
        for (int i4 = 0; i4 < trackCount; i4++) {
            Track track3 = tracks.valueAt(i4);
            this.trackBundles.get(track3.id).init(track3, getDefaultSampleValues(sparseArray, track3.id));
        }
    }

    private DefaultSampleValues getDefaultSampleValues(SparseArray<DefaultSampleValues> defaultSampleValuesArray, int trackId) {
        return defaultSampleValuesArray.size() == 1 ? defaultSampleValuesArray.valueAt(0) : (DefaultSampleValues) Assertions.checkNotNull(defaultSampleValuesArray.get(trackId));
    }

    private void onMoofContainerAtomRead(Atom.ContainerAtom moof) throws ParserException {
        parseMoof(moof, this.trackBundles, this.flags, this.extendedTypeScratch);
        DrmInitData drmInitData = this.sideloadedDrmInitData != null ? null : getDrmInitDataFromAtoms(moof.leafChildren);
        if (drmInitData != null) {
            int trackCount = this.trackBundles.size();
            for (int i = 0; i < trackCount; i++) {
                this.trackBundles.valueAt(i).updateDrmInitData(drmInitData);
            }
        }
        if (this.pendingSeekTimeUs != C.TIME_UNSET) {
            int trackCount2 = this.trackBundles.size();
            for (int i2 = 0; i2 < trackCount2; i2++) {
                this.trackBundles.valueAt(i2).seek(this.pendingSeekTimeUs);
            }
            this.pendingSeekTimeUs = C.TIME_UNSET;
        }
    }

    private void maybeInitExtraTracks() {
        TrackOutput[] trackOutputArr;
        if (this.emsgTrackOutputs == null) {
            this.emsgTrackOutputs = new TrackOutput[2];
            int emsgTrackOutputCount = 0;
            if (this.additionalEmsgTrackOutput != null) {
                int emsgTrackOutputCount2 = 0 + 1;
                this.emsgTrackOutputs[0] = this.additionalEmsgTrackOutput;
                emsgTrackOutputCount = emsgTrackOutputCount2;
            }
            if ((this.flags & 4) != 0) {
                this.emsgTrackOutputs[emsgTrackOutputCount] = this.extractorOutput.track(this.trackBundles.size(), 4);
                emsgTrackOutputCount++;
            }
            this.emsgTrackOutputs = (TrackOutput[]) Arrays.copyOf(this.emsgTrackOutputs, emsgTrackOutputCount);
            for (TrackOutput eventMessageTrackOutput : this.emsgTrackOutputs) {
                eventMessageTrackOutput.format(EMSG_FORMAT);
            }
        }
        if (this.cea608TrackOutputs == null) {
            this.cea608TrackOutputs = new TrackOutput[this.closedCaptionFormats.size()];
            for (int i = 0; i < this.cea608TrackOutputs.length; i++) {
                TrackOutput output = this.extractorOutput.track(this.trackBundles.size() + 1 + i, 3);
                output.format(this.closedCaptionFormats.get(i));
                this.cea608TrackOutputs[i] = output;
            }
        }
    }

    private void onEmsgLeafAtomRead(ParsableByteArray atom) {
        TrackOutput[] trackOutputArr;
        TrackOutput[] trackOutputArr2;
        if (this.emsgTrackOutputs != null && this.emsgTrackOutputs.length != 0) {
            atom.setPosition(12);
            int sampleSize = atom.bytesLeft();
            atom.readNullTerminatedString();
            atom.readNullTerminatedString();
            long timescale = atom.readUnsignedInt();
            long presentationTimeDeltaUs = Util.scaleLargeTimestamp(atom.readUnsignedInt(), 1000000L, timescale);
            for (TrackOutput emsgTrackOutput : this.emsgTrackOutputs) {
                atom.setPosition(12);
                emsgTrackOutput.sampleData(atom, sampleSize);
            }
            if (this.segmentIndexEarliestPresentationTimeUs != C.TIME_UNSET) {
                long sampleTimeUs = this.segmentIndexEarliestPresentationTimeUs + presentationTimeDeltaUs;
                if (this.timestampAdjuster != null) {
                    sampleTimeUs = this.timestampAdjuster.adjustSampleTimestamp(sampleTimeUs);
                }
                for (TrackOutput emsgTrackOutput2 : this.emsgTrackOutputs) {
                    emsgTrackOutput2.sampleMetadata(sampleTimeUs, 1, sampleSize, 0, null);
                }
                return;
            }
            this.pendingMetadataSampleInfos.addLast(new MetadataSampleInfo(presentationTimeDeltaUs, sampleSize));
            this.pendingMetadataSampleBytes += sampleSize;
        }
    }

    private static Pair<Integer, DefaultSampleValues> parseTrex(ParsableByteArray trex) {
        trex.setPosition(12);
        int trackId = trex.readInt();
        int defaultSampleDescriptionIndex = trex.readUnsignedIntToInt() - 1;
        int defaultSampleDuration = trex.readUnsignedIntToInt();
        int defaultSampleSize = trex.readUnsignedIntToInt();
        int defaultSampleFlags = trex.readInt();
        return Pair.create(Integer.valueOf(trackId), new DefaultSampleValues(defaultSampleDescriptionIndex, defaultSampleDuration, defaultSampleSize, defaultSampleFlags));
    }

    private static long parseMehd(ParsableByteArray mehd) {
        mehd.setPosition(8);
        int fullAtom = mehd.readInt();
        int version = Atom.parseFullAtomVersion(fullAtom);
        return version == 0 ? mehd.readUnsignedInt() : mehd.readUnsignedLongToLong();
    }

    private static void parseMoof(Atom.ContainerAtom moof, SparseArray<TrackBundle> trackBundleArray, int flags, byte[] extendedTypeScratch) throws ParserException {
        int moofContainerChildrenSize = moof.containerChildren.size();
        for (int i = 0; i < moofContainerChildrenSize; i++) {
            Atom.ContainerAtom child = moof.containerChildren.get(i);
            if (child.type == Atom.TYPE_traf) {
                parseTraf(child, trackBundleArray, flags, extendedTypeScratch);
            }
        }
    }

    private static void parseTraf(Atom.ContainerAtom traf, SparseArray<TrackBundle> trackBundleArray, int flags, byte[] extendedTypeScratch) throws ParserException {
        Atom.LeafAtom tfhd = traf.getLeafAtomOfType(Atom.TYPE_tfhd);
        TrackBundle trackBundle = parseTfhd(tfhd.data, trackBundleArray);
        if (trackBundle != null) {
            TrackFragment fragment = trackBundle.fragment;
            long decodeTime = fragment.nextFragmentDecodeTime;
            trackBundle.reset();
            Atom.LeafAtom tfdtAtom = traf.getLeafAtomOfType(Atom.TYPE_tfdt);
            if (tfdtAtom != null && (flags & 2) == 0) {
                decodeTime = parseTfdt(traf.getLeafAtomOfType(Atom.TYPE_tfdt).data);
            }
            parseTruns(traf, trackBundle, decodeTime, flags);
            TrackEncryptionBox encryptionBox = trackBundle.track.getSampleDescriptionEncryptionBox(fragment.header.sampleDescriptionIndex);
            Atom.LeafAtom saiz = traf.getLeafAtomOfType(Atom.TYPE_saiz);
            if (saiz != null) {
                parseSaiz(encryptionBox, saiz.data, fragment);
            }
            Atom.LeafAtom saio = traf.getLeafAtomOfType(Atom.TYPE_saio);
            if (saio != null) {
                parseSaio(saio.data, fragment);
            }
            Atom.LeafAtom senc = traf.getLeafAtomOfType(Atom.TYPE_senc);
            if (senc != null) {
                parseSenc(senc.data, fragment);
            }
            Atom.LeafAtom sbgp = traf.getLeafAtomOfType(Atom.TYPE_sbgp);
            Atom.LeafAtom sgpd = traf.getLeafAtomOfType(Atom.TYPE_sgpd);
            if (sbgp != null && sgpd != null) {
                parseSgpd(sbgp.data, sgpd.data, encryptionBox != null ? encryptionBox.schemeType : null, fragment);
            }
            int leafChildrenSize = traf.leafChildren.size();
            for (int i = 0; i < leafChildrenSize; i++) {
                Atom.LeafAtom atom = traf.leafChildren.get(i);
                if (atom.type == Atom.TYPE_uuid) {
                    parseUuid(atom.data, fragment, extendedTypeScratch);
                }
            }
        }
    }

    private static void parseTruns(Atom.ContainerAtom traf, TrackBundle trackBundle, long decodeTime, int flags) {
        int trunCount = 0;
        int totalSampleCount = 0;
        List<Atom.LeafAtom> leafChildren = traf.leafChildren;
        int leafChildrenSize = leafChildren.size();
        for (int i = 0; i < leafChildrenSize; i++) {
            Atom.LeafAtom atom = leafChildren.get(i);
            if (atom.type == Atom.TYPE_trun) {
                ParsableByteArray trunData = atom.data;
                trunData.setPosition(12);
                int trunSampleCount = trunData.readUnsignedIntToInt();
                if (trunSampleCount > 0) {
                    totalSampleCount += trunSampleCount;
                    trunCount++;
                }
            }
        }
        trackBundle.currentTrackRunIndex = 0;
        trackBundle.currentSampleInTrackRun = 0;
        trackBundle.currentSampleIndex = 0;
        trackBundle.fragment.initTables(trunCount, totalSampleCount);
        int trunIndex = 0;
        int trunStartPosition = 0;
        for (int i2 = 0; i2 < leafChildrenSize; i2++) {
            Atom.LeafAtom trun = leafChildren.get(i2);
            if (trun.type == Atom.TYPE_trun) {
                trunStartPosition = parseTrun(trackBundle, trunIndex, decodeTime, flags, trun.data, trunStartPosition);
                trunIndex++;
            }
        }
    }

    private static void parseSaiz(TrackEncryptionBox encryptionBox, ParsableByteArray saiz, TrackFragment out) throws ParserException {
        int vectorSize = encryptionBox.initializationVectorSize;
        saiz.setPosition(8);
        int fullAtom = saiz.readInt();
        int flags = Atom.parseFullAtomFlags(fullAtom);
        if ((flags & 1) == 1) {
            saiz.skipBytes(8);
        }
        int defaultSampleInfoSize = saiz.readUnsignedByte();
        int sampleCount = saiz.readUnsignedIntToInt();
        if (sampleCount != out.sampleCount) {
            throw new ParserException("Length mismatch: " + sampleCount + ", " + out.sampleCount);
        }
        int totalSize = 0;
        if (defaultSampleInfoSize == 0) {
            boolean[] sampleHasSubsampleEncryptionTable = out.sampleHasSubsampleEncryptionTable;
            for (int i = 0; i < sampleCount; i++) {
                int sampleInfoSize = saiz.readUnsignedByte();
                totalSize += sampleInfoSize;
                sampleHasSubsampleEncryptionTable[i] = sampleInfoSize > vectorSize;
            }
        } else {
            boolean subsampleEncryption = defaultSampleInfoSize > vectorSize;
            totalSize = 0 + (defaultSampleInfoSize * sampleCount);
            Arrays.fill(out.sampleHasSubsampleEncryptionTable, 0, sampleCount, subsampleEncryption);
        }
        out.initEncryptionData(totalSize);
    }

    private static void parseSaio(ParsableByteArray saio, TrackFragment out) throws ParserException {
        saio.setPosition(8);
        int fullAtom = saio.readInt();
        int flags = Atom.parseFullAtomFlags(fullAtom);
        if ((flags & 1) == 1) {
            saio.skipBytes(8);
        }
        int entryCount = saio.readUnsignedIntToInt();
        if (entryCount != 1) {
            throw new ParserException("Unexpected saio entry count: " + entryCount);
        }
        int version = Atom.parseFullAtomVersion(fullAtom);
        out.auxiliaryDataPosition = (version == 0 ? saio.readUnsignedInt() : saio.readUnsignedLongToLong()) + out.auxiliaryDataPosition;
    }

    private static TrackBundle parseTfhd(ParsableByteArray tfhd, SparseArray<TrackBundle> trackBundles) {
        tfhd.setPosition(8);
        int fullAtom = tfhd.readInt();
        int atomFlags = Atom.parseFullAtomFlags(fullAtom);
        int trackId = tfhd.readInt();
        TrackBundle trackBundle = getTrackBundle(trackBundles, trackId);
        if (trackBundle == null) {
            return null;
        }
        if ((atomFlags & 1) != 0) {
            long baseDataPosition = tfhd.readUnsignedLongToLong();
            trackBundle.fragment.dataPosition = baseDataPosition;
            trackBundle.fragment.auxiliaryDataPosition = baseDataPosition;
        }
        DefaultSampleValues defaultSampleValues = trackBundle.defaultSampleValues;
        int defaultSampleDescriptionIndex = (atomFlags & 2) != 0 ? tfhd.readUnsignedIntToInt() - 1 : defaultSampleValues.sampleDescriptionIndex;
        int defaultSampleDuration = (atomFlags & 8) != 0 ? tfhd.readUnsignedIntToInt() : defaultSampleValues.duration;
        int defaultSampleSize = (atomFlags & 16) != 0 ? tfhd.readUnsignedIntToInt() : defaultSampleValues.size;
        int defaultSampleFlags = (atomFlags & 32) != 0 ? tfhd.readUnsignedIntToInt() : defaultSampleValues.flags;
        trackBundle.fragment.header = new DefaultSampleValues(defaultSampleDescriptionIndex, defaultSampleDuration, defaultSampleSize, defaultSampleFlags);
        return trackBundle;
    }

    @Nullable
    private static TrackBundle getTrackBundle(SparseArray<TrackBundle> trackBundles, int trackId) {
        return trackBundles.size() == 1 ? trackBundles.valueAt(0) : trackBundles.get(trackId);
    }

    private static long parseTfdt(ParsableByteArray tfdt) {
        tfdt.setPosition(8);
        int fullAtom = tfdt.readInt();
        int version = Atom.parseFullAtomVersion(fullAtom);
        return version == 1 ? tfdt.readUnsignedLongToLong() : tfdt.readUnsignedInt();
    }

    private static int parseTrun(TrackBundle trackBundle, int index, long decodeTime, int flags, ParsableByteArray trun, int trackRunStart) {
        int sampleFlags;
        trun.setPosition(8);
        int fullAtom = trun.readInt();
        int atomFlags = Atom.parseFullAtomFlags(fullAtom);
        Track track = trackBundle.track;
        TrackFragment fragment = trackBundle.fragment;
        DefaultSampleValues defaultSampleValues = fragment.header;
        fragment.trunLength[index] = trun.readUnsignedIntToInt();
        fragment.trunDataPosition[index] = fragment.dataPosition;
        if ((atomFlags & 1) != 0) {
            long[] jArr = fragment.trunDataPosition;
            jArr[index] = jArr[index] + trun.readInt();
        }
        boolean firstSampleFlagsPresent = (atomFlags & 4) != 0;
        int firstSampleFlags = defaultSampleValues.flags;
        if (firstSampleFlagsPresent) {
            firstSampleFlags = trun.readUnsignedIntToInt();
        }
        boolean sampleDurationsPresent = (atomFlags & 256) != 0;
        boolean sampleSizesPresent = (atomFlags & 512) != 0;
        boolean sampleFlagsPresent = (atomFlags & 1024) != 0;
        boolean sampleCompositionTimeOffsetsPresent = (atomFlags & 2048) != 0;
        long edtsOffset = 0;
        if (track.editListDurations != null && track.editListDurations.length == 1 && track.editListDurations[0] == 0) {
            edtsOffset = Util.scaleLargeTimestamp(track.editListMediaTimes[0], 1000L, track.timescale);
        }
        int[] sampleSizeTable = fragment.sampleSizeTable;
        int[] sampleCompositionTimeOffsetTable = fragment.sampleCompositionTimeOffsetTable;
        long[] sampleDecodingTimeTable = fragment.sampleDecodingTimeTable;
        boolean[] sampleIsSyncFrameTable = fragment.sampleIsSyncFrameTable;
        boolean workaroundEveryVideoFrameIsSyncFrame = track.type == 2 && (flags & 1) != 0;
        int trackRunEnd = trackRunStart + fragment.trunLength[index];
        long timescale = track.timescale;
        long cumulativeTime = index > 0 ? fragment.nextFragmentDecodeTime : decodeTime;
        int i = trackRunStart;
        while (i < trackRunEnd) {
            int sampleDuration = sampleDurationsPresent ? trun.readUnsignedIntToInt() : defaultSampleValues.duration;
            int sampleSize = sampleSizesPresent ? trun.readUnsignedIntToInt() : defaultSampleValues.size;
            if (i == 0 && firstSampleFlagsPresent) {
                sampleFlags = firstSampleFlags;
            } else {
                sampleFlags = sampleFlagsPresent ? trun.readInt() : defaultSampleValues.flags;
            }
            if (sampleCompositionTimeOffsetsPresent) {
                int sampleOffset = trun.readInt();
                sampleCompositionTimeOffsetTable[i] = (int) ((sampleOffset * 1000) / timescale);
            } else {
                sampleCompositionTimeOffsetTable[i] = 0;
            }
            sampleDecodingTimeTable[i] = Util.scaleLargeTimestamp(cumulativeTime, 1000L, timescale) - edtsOffset;
            sampleSizeTable[i] = sampleSize;
            sampleIsSyncFrameTable[i] = ((sampleFlags >> 16) & 1) == 0 && (!workaroundEveryVideoFrameIsSyncFrame || i == 0);
            cumulativeTime += sampleDuration;
            i++;
        }
        fragment.nextFragmentDecodeTime = cumulativeTime;
        return trackRunEnd;
    }

    private static void parseUuid(ParsableByteArray uuid, TrackFragment out, byte[] extendedTypeScratch) throws ParserException {
        uuid.setPosition(8);
        uuid.readBytes(extendedTypeScratch, 0, 16);
        if (Arrays.equals(extendedTypeScratch, PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE)) {
            parseSenc(uuid, 16, out);
        }
    }

    private static void parseSenc(ParsableByteArray senc, TrackFragment out) throws ParserException {
        parseSenc(senc, 0, out);
    }

    private static void parseSenc(ParsableByteArray senc, int offset, TrackFragment out) throws ParserException {
        senc.setPosition(offset + 8);
        int fullAtom = senc.readInt();
        int flags = Atom.parseFullAtomFlags(fullAtom);
        if ((flags & 1) != 0) {
            throw new ParserException("Overriding TrackEncryptionBox parameters is unsupported.");
        }
        boolean subsampleEncryption = (flags & 2) != 0;
        int sampleCount = senc.readUnsignedIntToInt();
        if (sampleCount != out.sampleCount) {
            throw new ParserException("Length mismatch: " + sampleCount + ", " + out.sampleCount);
        }
        Arrays.fill(out.sampleHasSubsampleEncryptionTable, 0, sampleCount, subsampleEncryption);
        out.initEncryptionData(senc.bytesLeft());
        out.fillEncryptionData(senc);
    }

    private static void parseSgpd(ParsableByteArray sbgp, ParsableByteArray sgpd, String schemeType, TrackFragment out) throws ParserException {
        sbgp.setPosition(8);
        int sbgpFullAtom = sbgp.readInt();
        if (sbgp.readInt() == SAMPLE_GROUP_TYPE_seig) {
            if (Atom.parseFullAtomVersion(sbgpFullAtom) == 1) {
                sbgp.skipBytes(4);
            }
            if (sbgp.readInt() != 1) {
                throw new ParserException("Entry count in sbgp != 1 (unsupported).");
            }
            sgpd.setPosition(8);
            int sgpdFullAtom = sgpd.readInt();
            if (sgpd.readInt() == SAMPLE_GROUP_TYPE_seig) {
                int sgpdVersion = Atom.parseFullAtomVersion(sgpdFullAtom);
                if (sgpdVersion == 1) {
                    if (sgpd.readUnsignedInt() == 0) {
                        throw new ParserException("Variable length description in sgpd found (unsupported)");
                    }
                } else if (sgpdVersion >= 2) {
                    sgpd.skipBytes(4);
                }
                if (sgpd.readUnsignedInt() != 1) {
                    throw new ParserException("Entry count in sgpd != 1 (unsupported).");
                }
                sgpd.skipBytes(1);
                int patternByte = sgpd.readUnsignedByte();
                int cryptByteBlock = (patternByte & 240) >> 4;
                int skipByteBlock = patternByte & 15;
                boolean isProtected = sgpd.readUnsignedByte() == 1;
                if (isProtected) {
                    int perSampleIvSize = sgpd.readUnsignedByte();
                    byte[] keyId = new byte[16];
                    sgpd.readBytes(keyId, 0, keyId.length);
                    byte[] constantIv = null;
                    if (isProtected && perSampleIvSize == 0) {
                        int constantIvSize = sgpd.readUnsignedByte();
                        constantIv = new byte[constantIvSize];
                        sgpd.readBytes(constantIv, 0, constantIvSize);
                    }
                    out.definesEncryptionData = true;
                    out.trackEncryptionBox = new TrackEncryptionBox(isProtected, schemeType, perSampleIvSize, keyId, cryptByteBlock, skipByteBlock, constantIv);
                }
            }
        }
    }

    private static Pair<Long, ChunkIndex> parseSidx(ParsableByteArray atom, long inputPosition) throws ParserException {
        long earliestPresentationTime;
        long offset;
        atom.setPosition(8);
        int fullAtom = atom.readInt();
        int version = Atom.parseFullAtomVersion(fullAtom);
        atom.skipBytes(4);
        long timescale = atom.readUnsignedInt();
        if (version == 0) {
            earliestPresentationTime = atom.readUnsignedInt();
            offset = inputPosition + atom.readUnsignedInt();
        } else {
            earliestPresentationTime = atom.readUnsignedLongToLong();
            offset = inputPosition + atom.readUnsignedLongToLong();
        }
        long earliestPresentationTimeUs = Util.scaleLargeTimestamp(earliestPresentationTime, 1000000L, timescale);
        atom.skipBytes(2);
        int referenceCount = atom.readUnsignedShort();
        int[] sizes = new int[referenceCount];
        long[] offsets = new long[referenceCount];
        long[] durationsUs = new long[referenceCount];
        long[] timesUs = new long[referenceCount];
        long time = earliestPresentationTime;
        long timeUs = earliestPresentationTimeUs;
        for (int i = 0; i < referenceCount; i++) {
            int firstInt = atom.readInt();
            int type = Integer.MIN_VALUE & firstInt;
            if (type != 0) {
                throw new ParserException("Unhandled indirect reference");
            }
            long referenceDuration = atom.readUnsignedInt();
            sizes[i] = Integer.MAX_VALUE & firstInt;
            offsets[i] = offset;
            timesUs[i] = timeUs;
            time += referenceDuration;
            timeUs = Util.scaleLargeTimestamp(time, 1000000L, timescale);
            durationsUs[i] = timeUs - timesUs[i];
            atom.skipBytes(4);
            offset += sizes[i];
        }
        return Pair.create(Long.valueOf(earliestPresentationTimeUs), new ChunkIndex(sizes, offsets, durationsUs, timesUs));
    }

    private void readEncryptionData(ExtractorInput input) throws IOException, InterruptedException {
        TrackBundle nextTrackBundle = null;
        long nextDataOffset = Long.MAX_VALUE;
        int trackBundlesSize = this.trackBundles.size();
        for (int i = 0; i < trackBundlesSize; i++) {
            TrackFragment trackFragment = this.trackBundles.valueAt(i).fragment;
            if (trackFragment.sampleEncryptionDataNeedsFill && trackFragment.auxiliaryDataPosition < nextDataOffset) {
                nextDataOffset = trackFragment.auxiliaryDataPosition;
                TrackBundle nextTrackBundle2 = this.trackBundles.valueAt(i);
                nextTrackBundle = nextTrackBundle2;
            }
        }
        if (nextTrackBundle == null) {
            this.parserState = 3;
            return;
        }
        int bytesToSkip = (int) (nextDataOffset - input.getPosition());
        if (bytesToSkip < 0) {
            throw new ParserException("Offset to encryption data was negative.");
        }
        input.skipFully(bytesToSkip);
        nextTrackBundle.fragment.fillEncryptionData(input);
    }

    private boolean readSample(ExtractorInput input) throws IOException, InterruptedException {
        int writtenBytes;
        if (this.parserState == 3) {
            if (this.currentTrackBundle == null) {
                TrackBundle currentTrackBundle = getNextFragmentRun(this.trackBundles);
                if (currentTrackBundle == null) {
                    int bytesToSkip = (int) (this.endOfMdatPosition - input.getPosition());
                    if (bytesToSkip < 0) {
                        throw new ParserException("Offset to end of mdat was negative.");
                    }
                    input.skipFully(bytesToSkip);
                    enterReadingAtomHeaderState();
                    return false;
                }
                long nextDataPosition = currentTrackBundle.fragment.trunDataPosition[currentTrackBundle.currentTrackRunIndex];
                int bytesToSkip2 = (int) (nextDataPosition - input.getPosition());
                if (bytesToSkip2 < 0) {
                    Log.w(TAG, "Ignoring negative offset to sample data.");
                    bytesToSkip2 = 0;
                }
                input.skipFully(bytesToSkip2);
                this.currentTrackBundle = currentTrackBundle;
            }
            this.sampleSize = this.currentTrackBundle.fragment.sampleSizeTable[this.currentTrackBundle.currentSampleIndex];
            if (this.currentTrackBundle.currentSampleIndex < this.currentTrackBundle.firstSampleToOutputIndex) {
                input.skipFully(this.sampleSize);
                this.currentTrackBundle.skipSampleEncryptionData();
                if (!this.currentTrackBundle.next()) {
                    this.currentTrackBundle = null;
                }
                this.parserState = 3;
                return true;
            }
            if (this.currentTrackBundle.track.sampleTransformation == 1) {
                this.sampleSize -= 8;
                input.skipFully(8);
            }
            this.sampleBytesWritten = this.currentTrackBundle.outputSampleEncryptionData();
            this.sampleSize += this.sampleBytesWritten;
            this.parserState = 4;
            this.sampleCurrentNalBytesRemaining = 0;
        }
        TrackFragment fragment = this.currentTrackBundle.fragment;
        Track track = this.currentTrackBundle.track;
        TrackOutput output = this.currentTrackBundle.output;
        int sampleIndex = this.currentTrackBundle.currentSampleIndex;
        long sampleTimeUs = fragment.getSamplePresentationTime(sampleIndex) * 1000;
        if (this.timestampAdjuster != null) {
            sampleTimeUs = this.timestampAdjuster.adjustSampleTimestamp(sampleTimeUs);
        }
        if (track.nalUnitLengthFieldLength != 0) {
            byte[] nalPrefixData = this.nalPrefix.data;
            nalPrefixData[0] = 0;
            nalPrefixData[1] = 0;
            nalPrefixData[2] = 0;
            int nalUnitPrefixLength = track.nalUnitLengthFieldLength + 1;
            int nalUnitLengthFieldLengthDiff = 4 - track.nalUnitLengthFieldLength;
            while (this.sampleBytesWritten < this.sampleSize) {
                if (this.sampleCurrentNalBytesRemaining == 0) {
                    input.readFully(nalPrefixData, nalUnitLengthFieldLengthDiff, nalUnitPrefixLength);
                    this.nalPrefix.setPosition(0);
                    this.sampleCurrentNalBytesRemaining = this.nalPrefix.readUnsignedIntToInt() - 1;
                    this.nalStartCode.setPosition(0);
                    output.sampleData(this.nalStartCode, 4);
                    output.sampleData(this.nalPrefix, 1);
                    this.processSeiNalUnitPayload = this.cea608TrackOutputs.length > 0 && NalUnitUtil.isNalUnitSei(track.format.sampleMimeType, nalPrefixData[4]);
                    this.sampleBytesWritten += 5;
                    this.sampleSize += nalUnitLengthFieldLengthDiff;
                } else {
                    if (this.processSeiNalUnitPayload) {
                        this.nalBuffer.reset(this.sampleCurrentNalBytesRemaining);
                        input.readFully(this.nalBuffer.data, 0, this.sampleCurrentNalBytesRemaining);
                        output.sampleData(this.nalBuffer, this.sampleCurrentNalBytesRemaining);
                        writtenBytes = this.sampleCurrentNalBytesRemaining;
                        int unescapedLength = NalUnitUtil.unescapeStream(this.nalBuffer.data, this.nalBuffer.limit());
                        this.nalBuffer.setPosition(MimeTypes.VIDEO_H265.equals(track.format.sampleMimeType) ? 1 : 0);
                        this.nalBuffer.setLimit(unescapedLength);
                        CeaUtil.consume(sampleTimeUs, this.nalBuffer, this.cea608TrackOutputs);
                    } else {
                        writtenBytes = output.sampleData(input, this.sampleCurrentNalBytesRemaining, false);
                    }
                    this.sampleBytesWritten += writtenBytes;
                    this.sampleCurrentNalBytesRemaining -= writtenBytes;
                }
            }
        } else {
            while (this.sampleBytesWritten < this.sampleSize) {
                int writtenBytes2 = output.sampleData(input, this.sampleSize - this.sampleBytesWritten, false);
                this.sampleBytesWritten += writtenBytes2;
            }
        }
        int sampleFlags = fragment.sampleIsSyncFrameTable[sampleIndex] ? 1 : 0;
        TrackOutput.CryptoData cryptoData = null;
        if (fragment.definesEncryptionData) {
            sampleFlags |= 1073741824;
            TrackEncryptionBox encryptionBox = fragment.trackEncryptionBox != null ? fragment.trackEncryptionBox : track.getSampleDescriptionEncryptionBox(fragment.header.sampleDescriptionIndex);
            cryptoData = encryptionBox.cryptoData;
        }
        output.sampleMetadata(sampleTimeUs, sampleFlags, this.sampleSize, 0, cryptoData);
        outputPendingMetadataSamples(sampleTimeUs);
        if (!this.currentTrackBundle.next()) {
            this.currentTrackBundle = null;
        }
        this.parserState = 3;
        return true;
    }

    private void outputPendingMetadataSamples(long sampleTimeUs) {
        TrackOutput[] trackOutputArr;
        while (!this.pendingMetadataSampleInfos.isEmpty()) {
            MetadataSampleInfo sampleInfo = this.pendingMetadataSampleInfos.removeFirst();
            this.pendingMetadataSampleBytes -= sampleInfo.size;
            long metadataTimeUs = sampleTimeUs + sampleInfo.presentationTimeDeltaUs;
            if (this.timestampAdjuster != null) {
                metadataTimeUs = this.timestampAdjuster.adjustSampleTimestamp(metadataTimeUs);
            }
            for (TrackOutput emsgTrackOutput : this.emsgTrackOutputs) {
                emsgTrackOutput.sampleMetadata(metadataTimeUs, 1, sampleInfo.size, this.pendingMetadataSampleBytes, null);
            }
        }
    }

    private static TrackBundle getNextFragmentRun(SparseArray<TrackBundle> trackBundles) {
        TrackBundle nextTrackBundle = null;
        long nextTrackRunOffset = Long.MAX_VALUE;
        int trackBundlesSize = trackBundles.size();
        for (int i = 0; i < trackBundlesSize; i++) {
            TrackBundle trackBundle = trackBundles.valueAt(i);
            if (trackBundle.currentTrackRunIndex != trackBundle.fragment.trunCount) {
                long trunOffset = trackBundle.fragment.trunDataPosition[trackBundle.currentTrackRunIndex];
                if (trunOffset < nextTrackRunOffset) {
                    nextTrackBundle = trackBundle;
                    nextTrackRunOffset = trunOffset;
                }
            }
        }
        return nextTrackBundle;
    }

    private static DrmInitData getDrmInitDataFromAtoms(List<Atom.LeafAtom> leafChildren) {
        ArrayList<DrmInitData.SchemeData> schemeDatas = null;
        int leafChildrenSize = leafChildren.size();
        for (int i = 0; i < leafChildrenSize; i++) {
            Atom.LeafAtom child = leafChildren.get(i);
            if (child.type == Atom.TYPE_pssh) {
                if (schemeDatas == null) {
                    schemeDatas = new ArrayList<>();
                }
                byte[] psshData = child.data.data;
                UUID uuid = PsshAtomUtil.parseUuid(psshData);
                if (uuid == null) {
                    Log.w(TAG, "Skipped pssh atom (failed to extract uuid)");
                } else {
                    schemeDatas.add(new DrmInitData.SchemeData(uuid, MimeTypes.VIDEO_MP4, psshData));
                }
            }
        }
        if (schemeDatas == null) {
            return null;
        }
        return new DrmInitData(schemeDatas);
    }

    private static boolean shouldParseLeafAtom(int atom) {
        return atom == Atom.TYPE_hdlr || atom == Atom.TYPE_mdhd || atom == Atom.TYPE_mvhd || atom == Atom.TYPE_sidx || atom == Atom.TYPE_stsd || atom == Atom.TYPE_tfdt || atom == Atom.TYPE_tfhd || atom == Atom.TYPE_tkhd || atom == Atom.TYPE_trex || atom == Atom.TYPE_trun || atom == Atom.TYPE_pssh || atom == Atom.TYPE_saiz || atom == Atom.TYPE_saio || atom == Atom.TYPE_senc || atom == Atom.TYPE_uuid || atom == Atom.TYPE_sbgp || atom == Atom.TYPE_sgpd || atom == Atom.TYPE_elst || atom == Atom.TYPE_mehd || atom == Atom.TYPE_emsg;
    }

    private static boolean shouldParseContainerAtom(int atom) {
        return atom == Atom.TYPE_moov || atom == Atom.TYPE_trak || atom == Atom.TYPE_mdia || atom == Atom.TYPE_minf || atom == Atom.TYPE_stbl || atom == Atom.TYPE_moof || atom == Atom.TYPE_traf || atom == Atom.TYPE_mvex || atom == Atom.TYPE_edts;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class MetadataSampleInfo {
        public final long presentationTimeDeltaUs;
        public final int size;

        public MetadataSampleInfo(long presentationTimeDeltaUs, int size) {
            this.presentationTimeDeltaUs = presentationTimeDeltaUs;
            this.size = size;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class TrackBundle {
        public int currentSampleInTrackRun;
        public int currentSampleIndex;
        public int currentTrackRunIndex;
        public DefaultSampleValues defaultSampleValues;
        public int firstSampleToOutputIndex;
        public final TrackOutput output;
        public Track track;
        public final TrackFragment fragment = new TrackFragment();
        private final ParsableByteArray encryptionSignalByte = new ParsableByteArray(1);
        private final ParsableByteArray defaultInitializationVector = new ParsableByteArray();

        public TrackBundle(TrackOutput output) {
            this.output = output;
        }

        public void init(Track track, DefaultSampleValues defaultSampleValues) {
            this.track = (Track) Assertions.checkNotNull(track);
            this.defaultSampleValues = (DefaultSampleValues) Assertions.checkNotNull(defaultSampleValues);
            this.output.format(track.format);
            reset();
        }

        public void updateDrmInitData(DrmInitData drmInitData) {
            TrackEncryptionBox encryptionBox = this.track.getSampleDescriptionEncryptionBox(this.fragment.header.sampleDescriptionIndex);
            String schemeType = encryptionBox != null ? encryptionBox.schemeType : null;
            this.output.format(this.track.format.copyWithDrmInitData(drmInitData.copyWithSchemeType(schemeType)));
        }

        public void reset() {
            this.fragment.reset();
            this.currentSampleIndex = 0;
            this.currentTrackRunIndex = 0;
            this.currentSampleInTrackRun = 0;
            this.firstSampleToOutputIndex = 0;
        }

        public void seek(long timeUs) {
            long timeMs = C.usToMs(timeUs);
            for (int searchIndex = this.currentSampleIndex; searchIndex < this.fragment.sampleCount && this.fragment.getSamplePresentationTime(searchIndex) < timeMs; searchIndex++) {
                if (this.fragment.sampleIsSyncFrameTable[searchIndex]) {
                    this.firstSampleToOutputIndex = searchIndex;
                }
            }
        }

        public boolean next() {
            this.currentSampleIndex++;
            this.currentSampleInTrackRun++;
            if (this.currentSampleInTrackRun == this.fragment.trunLength[this.currentTrackRunIndex]) {
                this.currentTrackRunIndex++;
                this.currentSampleInTrackRun = 0;
                return false;
            }
            return true;
        }

        public int outputSampleEncryptionData() {
            ParsableByteArray initializationVectorData;
            int vectorSize;
            if (this.fragment.definesEncryptionData) {
                TrackEncryptionBox encryptionBox = getEncryptionBox();
                if (encryptionBox.initializationVectorSize != 0) {
                    initializationVectorData = this.fragment.sampleEncryptionData;
                    vectorSize = encryptionBox.initializationVectorSize;
                } else {
                    byte[] initVectorData = encryptionBox.defaultInitializationVector;
                    this.defaultInitializationVector.reset(initVectorData, initVectorData.length);
                    initializationVectorData = this.defaultInitializationVector;
                    vectorSize = initVectorData.length;
                }
                boolean subsampleEncryption = this.fragment.sampleHasSubsampleEncryptionTable[this.currentSampleIndex];
                this.encryptionSignalByte.data[0] = (byte) ((subsampleEncryption ? 128 : 0) | vectorSize);
                this.encryptionSignalByte.setPosition(0);
                this.output.sampleData(this.encryptionSignalByte, 1);
                this.output.sampleData(initializationVectorData, vectorSize);
                if (!subsampleEncryption) {
                    return vectorSize + 1;
                }
                ParsableByteArray subsampleEncryptionData = this.fragment.sampleEncryptionData;
                int subsampleCount = subsampleEncryptionData.readUnsignedShort();
                subsampleEncryptionData.skipBytes(-2);
                int subsampleDataLength = (subsampleCount * 6) + 2;
                this.output.sampleData(subsampleEncryptionData, subsampleDataLength);
                return vectorSize + 1 + subsampleDataLength;
            }
            return 0;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void skipSampleEncryptionData() {
            if (this.fragment.definesEncryptionData) {
                ParsableByteArray sampleEncryptionData = this.fragment.sampleEncryptionData;
                TrackEncryptionBox encryptionBox = getEncryptionBox();
                if (encryptionBox.initializationVectorSize != 0) {
                    sampleEncryptionData.skipBytes(encryptionBox.initializationVectorSize);
                }
                if (this.fragment.sampleHasSubsampleEncryptionTable[this.currentSampleIndex]) {
                    sampleEncryptionData.skipBytes(sampleEncryptionData.readUnsignedShort() * 6);
                }
            }
        }

        private TrackEncryptionBox getEncryptionBox() {
            int sampleDescriptionIndex = this.fragment.header.sampleDescriptionIndex;
            return this.fragment.trackEncryptionBox != null ? this.fragment.trackEncryptionBox : this.track.getSampleDescriptionEncryptionBox(sampleDescriptionIndex);
        }
    }
}
